package com.bansify.ai.data.ai

import com.bansify.ai.core.net.Http
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** One turn of the conversation handed to a provider. */
data class AiTurn(val isUser: Boolean, val text: String)

class AiException(val statusCode: Int, message: String) : Exception(message)

/**
 * Streams a chat completion from a card-free provider.
 *
 * Gemini and every OpenAI-compatible provider are handled here; nothing else is needed
 * to talk to Groq, OpenRouter, Cloudflare Workers AI, Z.ai or Mistral.
 */
class AiChatClient(private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }) {

    suspend fun stream(
        provider: AiProvider,
        model: String,
        apiKey: String,
        systemPrompt: String?,
        turns: List<AiTurn>,
        shouldStop: () -> Boolean,
        onDelta: (String) -> Unit
    ) {
        val key = apiKey.trim()
        if (provider.requiresKey && key.isEmpty()) throw AiException(401, "No API key saved for ${provider.name}.")
        val accountId = if (provider.id == "cloudflare") key.substringBefore(':', "").ifBlank { null } else null
        val token = if (provider.id == "cloudflare") key.substringAfter(':', key) else key
        if (provider.id == "cloudflare" && accountId == null) {
            throw AiException(401, "Cloudflare needs ACCOUNT_ID:API_TOKEN in the key field.")
        }

        val headers = buildMap {
            put("Content-Type", "application/json")
            when (provider.wire) {
                Wire.GEMINI -> put("x-goog-api-key", token)
                Wire.OPENAI -> put("Authorization", "Bearer $token")
                Wire.GATEWAY -> Unit
            }
        }
        val body = when (provider.wire) {
            Wire.GEMINI -> geminiBody(systemPrompt, turns)
            Wire.OPENAI -> openAiBody(model, systemPrompt, turns)
            Wire.GATEWAY -> gatewayBody(model, turns)
        }

        var emitted = false
        val result = Http.postSse(
            url = provider.endpoint(model, accountId),
            headers = headers,
            body = body,
            shouldStop = shouldStop
        ) { payload ->
            val text = when (provider.wire) {
                Wire.GEMINI -> geminiDelta(payload)
                Wire.OPENAI -> openAiDelta(payload)
                Wire.GATEWAY -> gatewayDelta(payload)
            }
            if (text.isNotEmpty()) {
                emitted = true
                onDelta(text)
            }
        }

        if (!result.isSuccess) {
            throw AiException(result.code, describeError(provider, result.code, result.body))
        }
        if (!emitted) throw AiException(200, "${provider.name} returned an empty reply. Try another model.")
    }

    private fun geminiBody(systemPrompt: String?, turns: List<AiTurn>): String {
        val contents = buildJsonArray {
            turns.forEach { turn ->
                add(
                    buildJsonObject {
                        put("role", if (turn.isUser) "user" else "model")
                        put(
                            "parts",
                            buildJsonArray { add(buildJsonObject { put("text", turn.text) }) }
                        )
                    }
                )
            }
        }
        return buildJsonObject {
            put("contents", contents)
            if (!systemPrompt.isNullOrBlank()) {
                put(
                    "systemInstruction",
                    buildJsonObject {
                        put("parts", buildJsonArray { add(buildJsonObject { put("text", systemPrompt) }) })
                    }
                )
            }
            put("generationConfig", buildJsonObject { put("temperature", 0.7) })
        }.toString()
    }

    private fun openAiBody(model: String, systemPrompt: String?, turns: List<AiTurn>): String {
        val messages = buildJsonArray {
            if (!systemPrompt.isNullOrBlank()) {
                add(buildJsonObject { put("role", "system"); put("content", systemPrompt) })
            }
            turns.forEach { turn ->
                add(
                    buildJsonObject {
                        put("role", if (turn.isUser) "user" else "assistant")
                        put("content", turn.text)
                    }
                )
            }
        }
        return buildJsonObject {
            put("model", model)
            put("messages", messages)
            put("stream", true)
            put("temperature", 0.7)
        }.toString()
    }

    private fun gatewayBody(model: String, turns: List<AiTurn>): String {
        val messages = buildJsonArray {
            turns.forEach { turn ->
                add(
                    buildJsonObject {
                        put("role", if (turn.isUser) "user" else "assistant")
                        put("content", turn.text)
                    }
                )
            }
        }
        return buildJsonObject {
            put("model", model.ifBlank { "auto" })
            put("messages", messages)
        }.toString()
    }

    private fun gatewayDelta(payload: String): String {
        val root = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return ""
        return root["delta"]?.jsonPrimitive?.content.orEmpty()
    }

    private fun geminiDelta(payload: String): String {
        val root = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return ""
        val candidates = root["candidates"] as? JsonArray ?: return ""
        val first = candidates.firstOrNull()?.jsonObject ?: return ""
        val parts = first["content"]?.jsonObject?.get("parts") as? JsonArray ?: return ""
        return parts.joinToString("") { part ->
            part.jsonObject["text"]?.jsonPrimitive?.content.orEmpty()
        }
    }

    private fun openAiDelta(payload: String): String {
        val root = runCatching { json.parseToJsonElement(payload).jsonObject }.getOrNull() ?: return ""
        val choices = root["choices"] as? JsonArray ?: return ""
        val first = choices.firstOrNull()?.jsonObject ?: return ""
        val delta = first["delta"]?.jsonObject ?: return ""
        return delta["content"]?.jsonPrimitive?.content.orEmpty()
    }

    private fun describeError(provider: AiProvider, code: Int, body: String): String {
        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["error"]
                ?.let { element ->
                    (element as? JsonObject)?.get("message")?.jsonPrimitive?.content
                        ?: element.jsonPrimitive.content
                }
        }.getOrNull()
        val detail = message?.takeIf { it.isNotBlank() } ?: body.take(220)
        return when (code) {
            401, 403 -> "${provider.name} rejected the key (HTTP $code). $detail"
            404 -> "${provider.name} does not know that model (HTTP 404). $detail"
            429 -> "${provider.name} free limit reached (HTTP 429). $detail"
            else -> "${provider.name} error HTTP $code. $detail"
        }
    }
}
