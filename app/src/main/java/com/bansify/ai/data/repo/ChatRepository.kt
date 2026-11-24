package com.bansify.ai.data.repo

import com.bansify.ai.data.mock.ChatAttachment
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.MockChats
import com.bansify.ai.data.mock.MockUser
import com.bansify.ai.data.mock.TimeBucket
import com.bansify.ai.data.mock.UserProfile
import com.bansify.ai.auth.AiAuth
import com.bansify.ai.auth.AiAccessTokenProvider
import com.bansify.ai.data.remote.AiApiClient
import com.bansify.ai.data.remote.AiChatMessage
import com.bansify.ai.data.remote.ChatStreamResult
import com.bansify.ai.data.remote.RemoteConversation
import com.bansify.ai.data.remote.RemoteMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Chat state for the UI, backed by the Bansify AI Worker.
 *
 * Conversations live in the worker's D1 database, not on the device: the same
 * thread shows up on ai.bansify.com and in this app because both read the same
 * rows. Local state here is only the in-memory view the Compose screens render.
 */
class ChatRepository(
    private val auth: AiAccessTokenProvider = AiAuth(),
    private val api: AiApiClient = AiApiClient(auth),
) {
    private val _conversations = MutableStateFlow(MockChats.seedConversations)
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    /** local conversation id -> worker conversation id */
    private val serverIds = mutableMapOf<String, String>()

    fun serverIdFor(localId: String?): String? = localId?.let { serverIds[it] }

    /** True when the app can talk to the worker at all (a Bansify Account is signed in). */
    suspend fun hasSession(): Boolean = auth.accessToken() != null

    private val _activeConversationId = MutableStateFlow<String?>(MockChats.seedConversations.first().id)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private var lastDeleted: Pair<Int, Conversation>? = null

    /**
     * Loads the signed-in account's history from the worker.
     *
     * Falls back to whatever is already in memory when the call fails, so an
     * offline launch still shows the last seen list instead of an empty screen.
     */
    suspend fun loadConversations(simulateError: Boolean = false): Result<List<Conversation>> {
        if (simulateError) {
            return Result.failure(IllegalStateException("Unable to load chat history."))
        }
        val remote = api.listConversations()
        remote.onSuccess { summaries ->
            if (summaries.isEmpty()) return@onSuccess
            val merged = summaries.map { it.toConversation() }
            _conversations.value = merged
            summaries.forEach { summary -> serverIds[summary.id] = summary.id }
        }
        return remote.map { _conversations.value }
    }

    /**
     * Streams one assistant reply from the worker and reports each delta.
     *
     * The worker persists both turns when a conversation id is present, so the
     * reply survives a process death and appears on the web app too.
     */
    suspend fun streamAssistantReply(
        conversationId: String,
        prompt: String,
        modelId: String?,
        style: String?,
        onDelta: (String) -> Unit,
    ): Result<ChatStreamResult> {
        val conversation = _conversations.value.firstOrNull { it.id == conversationId }
            ?: return Result.failure(IllegalStateException("Conversation not found"))

        val history = conversation.messages
            .filter { !it.isStreaming && it.generatedMedia == null && it.content.isNotBlank() }
            .takeLast(MAX_HISTORY_TURNS)
            .map {
                AiChatMessage(
                    role = if (it.role == MessageRole.USER) "user" else "assistant",
                    content = it.content,
                )
            }
            .toMutableList()

        if (history.lastOrNull()?.content != prompt) {
            history += AiChatMessage(role = "user", content = prompt)
        }

        val result = api.streamChat(
            messages = history,
            conversationId = serverIds[conversationId],
            conversationTitle = conversation.title,
            model = modelId?.takeIf { it != "auto" },
            style = style,
            onDelta = onDelta,
        )
        result.onSuccess { streamed ->
            streamed.conversationId?.let { serverIds[conversationId] = it }
        }
        return result
    }

    /** Deletes the conversation on the server too, so it does not come back on refresh. */
    suspend fun deleteConversationEverywhere(conversationId: String) {
        serverIds[conversationId]?.let { api.deleteConversation(it) }
        serverIds.remove(conversationId)
        deleteConversation(conversationId)
    }

    /** Renames on the server so web and app agree. */
    suspend fun renameConversationEverywhere(conversationId: String, newTitle: String) {
        renameConversation(conversationId, newTitle)
        serverIds[conversationId]?.let { api.renameConversation(it, newTitle.trim()) }
    }

    /** Pulls one thread's messages from the worker (used when opening it from History). */
    suspend fun loadRemoteConversation(conversationId: String): Result<Conversation?> {
        val serverId = serverIds[conversationId]
            ?: return Result.success(_conversations.value.firstOrNull { it.id == conversationId })
        return api.getConversation(serverId).map { detail ->
            val messages = detail.messages.map { it.toChatMessage() }
            _conversations.update { list ->
                list.map { conv ->
                    if (conv.id == conversationId) {
                        conv.copy(
                            title = detail.conversation.title.ifBlank { conv.title },
                            preview = detail.conversation.preview,
                            messages = messages,
                        )
                    } else {
                        conv
                    }
                }
            }
            _conversations.value.firstOrNull { it.id == conversationId }
        }
    }

    suspend fun loadConversation(id: String, simulateError: Boolean = false): Result<Conversation?> {
        delay(620L)
        return if (simulateError) {
            Result.failure(IllegalStateException("Unable to load conversation (mock error)."))
        } else {
            Result.success(_conversations.value.firstOrNull { it.id == id })
        }
    }

    fun selectConversation(id: String?) {
        _activeConversationId.value = id
    }

    fun startNewBlankChat() {
        _activeConversationId.value = null
    }

    fun createPostOnboardConversation(profile: UserProfile): Conversation {
        val existing = _conversations.value.firstOrNull { it.id == "conv_welcome" }
        val opening = MockUser.postOnboardOpeningMessage(profile)
        val message = ChatMessage(
            id = "m_welcome_1",
            role = MessageRole.ASSISTANT,
            content = opening,
            timestampLabel = "Just now",
            dateGroup = "Today",
            modelName = "Auto ⚡"
        )
        val conv = Conversation(
            id = "conv_welcome",
            title = "Welcome",
            preview = opening.lineSequence().firstOrNull().orEmpty(),
            relativeTime = "Just now",
            timeBucket = TimeBucket.TODAY,
            modelId = "auto",
            modelLabel = "Auto ⚡",
            isPinned = false,
            messages = listOf(message)
        )
        _conversations.update { list ->
            if (existing != null) {
                list.map { if (it.id == conv.id) conv else it }
            } else {
                listOf(conv) + list
            }
        }
        _activeConversationId.value = conv.id
        return conv
    }

    fun ensureConversationForPrompt(
        prompt: String,
        modelId: String,
        modelLabel: String,
        attachments: List<ChatAttachment>,
        mode: ChatMode = ChatMode.CHAT
    ): Pair<String, ChatMessage> {
        val currentId = _activeConversationId.value
        val userMessage = ChatMessage(
            id = "msg_u_${System.currentTimeMillis()}",
            role = MessageRole.USER,
            content = prompt,
            timestampLabel = "Just now",
            dateGroup = "Today",
            attachments = attachments
        )
        if (currentId != null && _conversations.value.any { it.id == currentId }) {
            appendMessage(currentId, userMessage)
            return currentId to userMessage
        }
        val defaultTitle = when (mode) {
            ChatMode.CHAT -> "New chat"
            ChatMode.IMAGE -> "New image"
            ChatMode.VIDEO -> "New video"
        }
        val title = prompt.trim().take(30).ifEmpty { defaultTitle }
        val newId = "conv_${System.currentTimeMillis()}"
        val newConv = Conversation(
            id = newId,
            title = title,
            preview = prompt.take(80),
            relativeTime = "Just now",
            timeBucket = TimeBucket.TODAY,
            modelId = modelId,
            modelLabel = modelLabel,
            mode = mode,
            messages = listOf(userMessage)
        )
        _conversations.update { listOf(newConv) + it }
        _activeConversationId.value = newId
        return newId to userMessage
    }

    fun appendMessage(conversationId: String, message: ChatMessage) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(
                        messages = conv.messages + message,
                        preview = message.content.replace("\n", " ").take(90),
                        relativeTime = "Just now",
                        timeBucket = TimeBucket.TODAY
                    )
                } else conv
            }
        }
    }

    fun updateMessage(conversationId: String, messageId: String, transform: (ChatMessage) -> ChatMessage) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    val updated = conv.messages.map { msg ->
                        if (msg.id == messageId) transform(msg) else msg
                    }
                    val lastText = updated.lastOrNull()?.content.orEmpty()
                    conv.copy(
                        messages = updated,
                        preview = lastText.replace("\n", " ").take(90)
                    )
                } else conv
            }
        }
    }

    fun setMessageFeedback(conversationId: String, messageId: String, feedback: MessageFeedback) {
        updateMessage(conversationId, messageId) { msg ->
            msg.copy(feedback = if (msg.feedback == feedback) MessageFeedback.NONE else feedback)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        val clean = newTitle.trim()
        if (clean.isEmpty()) return
        _conversations.update { list ->
            list.map { if (it.id == id) it.copy(title = clean) else it }
        }
    }

    fun togglePin(id: String) {
        _conversations.update { list ->
            list.map { if (it.id == id) it.copy(isPinned = !it.isPinned) else it }
        }
    }

    fun duplicateConversation(id: String) {
        val source = _conversations.value.firstOrNull { it.id == id } ?: return
        val copy = source.copy(
            id = "conv_dup_${System.currentTimeMillis()}",
            title = "${source.title} (copy)",
            relativeTime = "Just now",
            timeBucket = TimeBucket.TODAY,
            isPinned = false
        )
        _conversations.update { listOf(copy) + it }
    }

    fun deleteConversation(id: String): Conversation? {
        val index = _conversations.value.indexOfFirst { it.id == id }
        if (index < 0) return null
        val target = _conversations.value[index]
        lastDeleted = index to target
        _conversations.update { it.filterNot { conv -> conv.id == id } }
        if (_activeConversationId.value == id) {
            _activeConversationId.value = null
        }
        return target
    }

    fun undoDelete() {
        val saved = lastDeleted ?: return
        lastDeleted = null
        _conversations.update { current ->
            val mutable = current.toMutableList()
            val insertAt = saved.first.coerceIn(0, mutable.size)
            mutable.add(insertAt, saved.second)
            mutable
        }
    }

    fun deleteMultiple(ids: Set<String>) {
        _conversations.update { it.filterNot { conv -> conv.id in ids } }
        if (_activeConversationId.value in ids) {
            _activeConversationId.value = null
        }
    }

    fun pinMultiple(ids: Set<String>) {
        _conversations.update { list ->
            list.map { if (it.id in ids) it.copy(isPinned = true) else it }
        }
    }

    fun clearActiveConversationMessages() {
        val id = _activeConversationId.value ?: return
        _conversations.update { list ->
            list.map { if (it.id == id) it.copy(messages = emptyList(), preview = "Chat cleared") else it }
        }
    }

    fun deleteAllConversations() {
        _conversations.value = emptyList()
        _activeConversationId.value = null
        serverIds.clear()
    }

    /** Wipes the account's history in D1 as well as in memory. */
    suspend fun deleteAllConversationsEverywhere() {
        api.deleteAllConversations()
        deleteAllConversations()
    }

    private fun RemoteConversation.toConversation(): Conversation = Conversation(
        id = id,
        title = title.ifBlank { "New chat" },
        preview = preview,
        relativeTime = relativeLabel(updatedAt),
        timeBucket = bucketFor(updatedAt),
        modelId = modelId ?: "auto",
        modelLabel = modelId ?: "Auto ⚡",
        messages = emptyList(),
    )

    private fun RemoteMessage.toChatMessage(): ChatMessage = ChatMessage(
        id = id,
        role = if (role == "user") MessageRole.USER else MessageRole.ASSISTANT,
        content = content,
        timestampLabel = relativeLabel(createdAt),
        dateGroup = TimeBucket.TODAY.label,
    )

    private fun bucketFor(epochMillis: Long): TimeBucket {
        val ageHours = ((System.currentTimeMillis() - epochMillis).coerceAtLeast(0L)) / 3_600_000L
        return when {
            ageHours < 24 -> TimeBucket.TODAY
            ageHours < 48 -> TimeBucket.YESTERDAY
            ageHours < 24 * 7 -> TimeBucket.LAST_7_DAYS
            else -> TimeBucket.OLDER
        }
    }

    private fun relativeLabel(epochMillis: Long): String {
        val minutes = ((System.currentTimeMillis() - epochMillis).coerceAtLeast(0L)) / 60_000L
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 24 * 60 -> "${minutes / 60}h ago"
            else -> "${minutes / (24 * 60)}d ago"
        }
    }

    private companion object {
        const val MAX_HISTORY_TURNS = 12
    }
}
