package com.bansify.ai.data.repo

import android.content.Context
import com.bansify.ai.data.chat.ChatStore
import com.bansify.ai.data.mock.ChatAttachment
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.TimeBucket
import com.bansify.ai.data.mock.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Owns the chat list and writes it to disk after every change, so history survives restarts.
 * Replies are produced by [com.bansify.ai.data.ai.AiChatClient]; this class stores them.
 */
class ChatRepository(context: Context) {

    private val store = ChatStore(File(context.applicationContext.filesDir, "chats.json"))
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private var lastDeleted: Pair<Int, Conversation>? = null

    init {
        ioScope.launch {
            val loaded = store.load()
            _conversations.value = loaded
            if (_activeConversationId.value == null) {
                _activeConversationId.value = loaded.firstOrNull()?.id
            }
        }
    }

    suspend fun loadConversations(simulateError: Boolean = false): Result<List<Conversation>> =
        if (simulateError) {
            Result.failure(IllegalStateException("Could not read chat history."))
        } else {
            Result.success(_conversations.value)
        }

    suspend fun loadConversation(id: String, simulateError: Boolean = false): Result<Conversation?> =
        if (simulateError) {
            Result.failure(IllegalStateException("Could not open that chat."))
        } else {
            Result.success(_conversations.value.firstOrNull { it.id == id })
        }

    fun selectConversation(id: String?) {
        _activeConversationId.value = id
    }

    fun startNewBlankChat() {
        _activeConversationId.value = null
    }

    /** Creates the empty conversation the chat screen opens after onboarding. */
    fun createPostOnboardConversation(profile: UserProfile): Conversation {
        val existing = _conversations.value.firstOrNull { it.id == "conv_welcome" }
        if (existing != null) {
            _activeConversationId.value = existing.id
            return existing
        }
        val conversation = Conversation(
            id = "conv_welcome",
            title = if (profile.name.isBlank()) "New chat" else "Hello, ${profile.name.take(18)}",
            preview = "",
            relativeTime = "Just now",
            timeBucket = TimeBucket.TODAY,
            modelId = "auto",
            modelLabel = "Auto",
            isPinned = false,
            messages = emptyList()
        )
        _conversations.update { listOf(conversation) + it }
        _activeConversationId.value = conversation.id
        persist()
        return conversation
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
        val newConversation = Conversation(
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
        _conversations.update { listOf(newConversation) + it }
        _activeConversationId.value = newId
        persist()
        return newId to userMessage
    }

    fun appendMessage(conversationId: String, message: ChatMessage) {
        _conversations.update { list ->
            list.map { conversation ->
                if (conversation.id == conversationId) {
                    conversation.copy(
                        messages = conversation.messages + message,
                        preview = message.content.replace("\n", " ").take(90),
                        relativeTime = "Just now",
                        timeBucket = TimeBucket.TODAY
                    )
                } else {
                    conversation
                }
            }
        }
        persist()
    }

    fun updateMessage(
        conversationId: String,
        messageId: String,
        persist: Boolean = true,
        transform: (ChatMessage) -> ChatMessage
    ) {
        _conversations.update { list ->
            list.map { conversation ->
                if (conversation.id == conversationId) {
                    val updated = conversation.messages.map { message ->
                        if (message.id == messageId) transform(message) else message
                    }
                    val lastText = updated.lastOrNull()?.content.orEmpty()
                    conversation.copy(
                        messages = updated,
                        preview = lastText.replace("\n", " ").take(90)
                    )
                } else {
                    conversation
                }
            }
        }
        if (persist) persist()
    }

    fun setMessageFeedback(conversationId: String, messageId: String, feedback: MessageFeedback) {
        updateMessage(conversationId, messageId) { message ->
            message.copy(feedback = if (message.feedback == feedback) MessageFeedback.NONE else feedback)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        val clean = newTitle.trim()
        if (clean.isEmpty()) return
        _conversations.update { list -> list.map { if (it.id == id) it.copy(title = clean) else it } }
        persist()
    }

    fun togglePin(id: String) {
        _conversations.update { list -> list.map { if (it.id == id) it.copy(isPinned = !it.isPinned) else it } }
        persist()
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
        persist()
    }

    fun deleteConversation(id: String): Conversation? {
        val index = _conversations.value.indexOfFirst { it.id == id }
        if (index < 0) return null
        val target = _conversations.value[index]
        lastDeleted = index to target
        _conversations.update { list -> list.filterNot { it.id == id } }
        if (_activeConversationId.value == id) _activeConversationId.value = null
        persist()
        return target
    }

    fun undoDelete() {
        val saved = lastDeleted ?: return
        lastDeleted = null
        _conversations.update { current ->
            val mutable = current.toMutableList()
            mutable.add(saved.first.coerceIn(0, mutable.size), saved.second)
            mutable
        }
        persist()
    }

    fun deleteMultiple(ids: Set<String>) {
        _conversations.update { list -> list.filterNot { it.id in ids } }
        if (_activeConversationId.value in ids) _activeConversationId.value = null
        persist()
    }

    fun pinMultiple(ids: Set<String>) {
        _conversations.update { list -> list.map { if (it.id in ids) it.copy(isPinned = true) else it } }
        persist()
    }

    fun clearActiveConversationMessages() {
        val id = _activeConversationId.value ?: return
        _conversations.update { list ->
            list.map { if (it.id == id) it.copy(messages = emptyList(), preview = "Chat cleared") else it }
        }
        persist()
    }

    fun deleteAllConversations() {
        _conversations.value = emptyList()
        _activeConversationId.value = null
        persist()
    }

    private fun persist() {
        val snapshot = _conversations.value
        ioScope.launch { store.save(snapshot) }
    }
}
