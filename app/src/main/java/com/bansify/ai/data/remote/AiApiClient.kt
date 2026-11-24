package com.bansify.ai.data.remote

import com.bansify.ai.auth.AiAccessTokenProvider
import com.bansify.ai.auth.AiEndpoints
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/** One turn sent to the worker. Roles are the worker's own vocabulary. */
@Serializable
data class AiChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class RemoteMessage(
    val id: String,
    val role: String = "assistant",
    val content: String = "",
    @SerialName("created_at") val createdAt: Long = 0,
)

@Serializable
data class ConversationDetail(
    val conversation: RemoteConversation,
    val messages: List<RemoteMessage> = emptyList(),
)

@Serializable
private data class ConversationDetailResponse(
    val conversation: RemoteConversation,
    val messages: List<RemoteMessage> = emptyList(),
)

@Serializable
data class RemoteConversation(
    val id: String,
    val title: String = "New chat",
    val preview: String = "",
    @SerialName("model_id") val modelId: String? = null,
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("updated_at") val updatedAt: Long = 0,
)

@Serializable
private data class ConversationsResponse(val conversations: List<RemoteConversation> = emptyList())

@Serializable
private data class ConversationResponse(val conversation: RemoteConversation)

@Serializable
private data class ChatRequestBody(
    val messages: List<AiChatMessage>,
    val model: String? = null,
    val style: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("conversation_title") val conversationTitle: String? = null,
)

@Serializable
private data class SseDelta(val delta: String? = null)

@Serializable
data class AiUser(
    val id: String = "",
    val username: String = "",
    val email: String? = null,
    val name: String? = null,
)

@Serializable
private data class MeResponse(
    val authenticated: Boolean = false,
    val user: AiUser? = null,
)

/** Result of one streamed turn: the text plus the conversation the worker wrote it to. */
data class ChatStreamResult(
    val conversationId: String?,
    val text: String,
)

class AiApiException(
    val status: Int,
    val code: String,
    message: String,
) : IOException(message)

/**
 * Client for `https://ai.bansify.com/api` (the Bansify AI Worker).
 *
 * Security rules enforced here:
 * - The `Authorization: Bearer` header is attached only to [baseUrl]'s host.
 * - Redirect following is disabled, so a 30x can never move the request (and the
 *   token) somewhere else.
 * - `Accept: application/json` is always sent on JSON calls.
 */
class AiApiClient(
    private val tokenProvider: AiAccessTokenProvider,
    private val baseUrl: String = AiEndpoints.AI_API_BASE_URL,
    private val client: OkHttpClient = defaultClient(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val base: HttpUrl = baseUrl.trimEnd('/').toHttpUrl()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // --- Identity ------------------------------------------------------------

    /** Proves the stored token still works before the app shows the chat screen. */
    suspend fun me(): Result<AiUser> = call("GET", "/v1/me") { response ->
        val body = response.body?.string().orEmpty()
        val parsed = json.decodeFromString<MeResponse>(body)
        parsed.user ?: throw AiApiException(401, "auth_required", "Not signed in to Bansify AI")
    }

    // --- Conversations -------------------------------------------------------

    suspend fun listConversations(): Result<List<RemoteConversation>> =
        call("GET", "/v1/conversations") { response ->
            json.decodeFromString<ConversationsResponse>(response.body?.string().orEmpty()).conversations
        }

    suspend fun createConversation(title: String?, model: String? = null): Result<RemoteConversation> =
        call("POST", "/v1/conversations", body = conversationBody(title, model)) { response ->
            json.decodeFromString<ConversationResponse>(response.body?.string().orEmpty()).conversation
        }

    suspend fun getConversation(conversationId: String): Result<ConversationDetail> =
        call("GET", "/v1/conversations/$conversationId") { response ->
            val parsed = json.decodeFromString<ConversationDetailResponse>(response.body?.string().orEmpty())
            ConversationDetail(conversation = parsed.conversation, messages = parsed.messages)
        }

    suspend fun renameConversation(conversationId: String, title: String): Result<Unit> =
        call("PATCH", "/v1/conversations/$conversationId", body = conversationBody(title, null)) { }

    suspend fun deleteConversation(conversationId: String): Result<Unit> =
        call("DELETE", "/v1/conversations/$conversationId") { }

    suspend fun deleteAllConversations(): Result<Unit> =
        call("DELETE", "/v1/conversations") { }

    // --- Chat ----------------------------------------------------------------

    /**
     * Streams one assistant reply.
     *
     * When [conversationId] is null the worker creates a conversation and returns
     * its id in the `x-conversation-id` header, so the next turn lands in the same
     * thread on the server — that is what makes history identical on web and app.
     */
    suspend fun streamChat(
        messages: List<AiChatMessage>,
        conversationId: String?,
        conversationTitle: String?,
        model: String?,
        style: String?,
        onDelta: (String) -> Unit,
    ): Result<ChatStreamResult> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = ChatRequestBody(
                messages = messages,
                model = model,
                style = style,
                conversationId = conversationId,
                conversationTitle = conversationTitle,
            )
            val request = requestBuilder("/v1/chat")
                .post(json.encodeToString(ChatRequestBody.serializer(), payload).toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw toApiException(response)

                val serverConversationId = response.header("x-conversation-id") ?: conversationId
                val source = response.body?.source()
                    ?: throw AiApiException(502, "empty_stream", "Worker returned an empty stream")

                val text = StringBuilder()
                while (true) {
                    val line = source.readUtf8Line() ?: break
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("data:")) continue
                    val data = trimmed.removePrefix("data:").trim()
                    if (data.isEmpty()) continue
                    if (data == "[DONE]") break
                    val delta = runCatching { json.decodeFromString<SseDelta>(data).delta }.getOrNull()
                    if (!delta.isNullOrEmpty()) {
                        text.append(delta)
                        onDelta(delta)
                    }
                }
                ChatStreamResult(conversationId = serverConversationId, text = text.toString())
            }
        }
    }

    // --- Plumbing ------------------------------------------------------------

    private fun conversationBody(title: String?, model: String?): String =
        buildString {
            append('{')
            append("\"title\":").append(JsonPrimitive(title ?: "New chat"))
            if (model != null) append(",\"model\":").append(JsonPrimitive(model))
            append('}')
        }

    private fun requestBuilder(path: String): Request.Builder {
        val url = base.newBuilder().addPathSegments(path.trimStart('/')).build()
        requireSameHost(url)
        return Request.Builder()
            .url(url)
            .header("Accept", "application/json")
    }

    private suspend fun <T> call(
        method: String,
        path: String,
        body: String? = null,
        parse: (Response) -> T,
    ): Result<T> = withContext(Dispatchers.IO) {
        runCatching {
            val token = tokenProvider.accessToken()
                ?: throw AiApiException(401, "auth_required", "Not signed in to Bansify AI")

            val builder = requestBuilder(path).header("Authorization", "Bearer $token")
            // OkHttp rejects a POST/PATCH without a body, so send an empty JSON object.
            val request = when {
                body != null -> builder.method(method, body.toRequestBody(jsonMediaType)).build()
                method == "POST" || method == "PATCH" -> builder.method(method, "{}".toRequestBody(jsonMediaType)).build()
                else -> builder.method(method, null).build()
            }

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw toApiException(response)
                parse(response)
            }
        }
    }

    private fun toApiException(response: Response): AiApiException {
        val raw = response.body?.string().orEmpty()
        val message = runCatching {
            json.parseToJsonElement(raw)
                .let { element ->
                    (element as? kotlinx.serialization.json.JsonObject)
                        ?.get("error")
                        ?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
                }
        }.getOrNull()
        val code = when (response.code) {
            401 -> "auth_required"
            429 -> "quota_exhausted"
            503 -> "no_provider"
            else -> "request_failed"
        }
        return AiApiException(response.code, code, message ?: "Request failed (${response.code})")
    }

    /** Refuses any request that is not aimed at the AI Worker origin. */
    private fun requireSameHost(url: HttpUrl) {
        require(url.scheme == base.scheme && url.host == base.host && url.port == base.port) {
            "Refusing to send a Bansify AI request to ${url.host}"
        }
    }

    companion object {
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
