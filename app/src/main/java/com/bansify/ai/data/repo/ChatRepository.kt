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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatRepository {
    private val _conversations = MutableStateFlow(MockChats.seedConversations)
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(MockChats.seedConversations.first().id)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private var lastDeleted: Pair<Int, Conversation>? = null

    suspend fun loadConversations(simulateError: Boolean = false): Result<List<Conversation>> {
        delay(650L)
        return if (simulateError) {
            Result.failure(IllegalStateException("Unable to load chat history (mock error)."))
        } else {
            Result.success(_conversations.value)
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
    }
}
