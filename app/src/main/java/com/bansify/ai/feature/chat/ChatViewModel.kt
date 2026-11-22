package com.bansify.ai.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.data.mock.AppSettings
import com.bansify.ai.data.mock.ChatAttachment
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.data.mock.GeneratedMedia
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.MockChats
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.ai.AiChatClient
import com.bansify.ai.data.ai.AiException
import com.bansify.ai.data.ai.AiTurn
import com.bansify.ai.data.mock.UserProfile
import com.bansify.ai.data.repo.ChatRepository
import com.bansify.ai.data.repo.ModelRepository
import com.bansify.ai.data.repo.UserPreferencesRepository
import com.bansify.ai.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ChatScreenData(
    val conversation: Conversation?,
    val messages: List<ChatMessage>,
    val userProfile: UserProfile,
    val selectedModel: ModelItem,
    val settings: AppSettings,
    val chatMode: ChatMode = ChatMode.CHAT,
    val draftText: String = "",
    val pendingAttachments: List<ChatAttachment> = emptyList(),
    val isStreaming: Boolean = false,
    val isListeningVoice: Boolean = false,
    val inlineError: String? = null,
    val revealedActionsMessageId: String? = null
)

class ChatViewModel(
    private val chatRepo: ChatRepository,
    private val modelRepo: ModelRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val chatEngine: AiChatClient
) : ViewModel() {

    private val _isLoadingConversation = MutableStateFlow(false)
    private val _draftText = MutableStateFlow("")
    private val _pendingAttachments = MutableStateFlow<List<ChatAttachment>>(emptyList())
    private val _isStreaming = MutableStateFlow(false)
    private val _isListeningVoice = MutableStateFlow(false)
    private val _inlineError = MutableStateFlow<String?>(null)
    private val _revealedActionsId = MutableStateFlow<String?>(null)
    private val _lastFailedPrompt = MutableStateFlow<String?>(null)
    private val _chatMode = MutableStateFlow(ChatMode.CHAT)

    private var streamingJob: Job? = null
    private var mediaSeedCounter = 0

    val uiState: StateFlow<UiState<ChatScreenData>> = combine(
        combine(
            chatRepo.conversations,
            chatRepo.activeConversationId,
            prefsRepo.userProfile,
            prefsRepo.appSettings,
            modelRepo.selectedModelId
        ) { convs, activeId, profile, settings, selectedModelId ->
            val conv = activeId?.let { id -> convs.firstOrNull { it.id == id } }
            val model = modelRepo.findModel(selectedModelId)
            Triple(conv, profile, Pair(settings, model))
        },
        combine(
            _isLoadingConversation,
            _draftText,
            _pendingAttachments,
            _isStreaming,
            _isListeningVoice
        ) { loading, draft, attachments, streaming, listening ->
            arrayOf<Any>(loading, draft, attachments, streaming, listening)
        },
        combine(_inlineError, _revealedActionsId, _chatMode) { err, revealed, mode ->
            Triple(err, revealed, mode)
        }
    ) { core, uiFlags, extra ->
        val loading = uiFlags[0] as Boolean
        if (loading) {
            UiState.Loading
        } else {
            val conv = core.first
            val profile = core.second
            val settings = core.third.first
            val model = core.third.second
            @Suppress("UNCHECKED_CAST")
            val attachments = uiFlags[2] as List<ChatAttachment>
            UiState.Success(
                ChatScreenData(
                    conversation = conv,
                    messages = conv?.messages.orEmpty(),
                    userProfile = profile,
                    selectedModel = model,
                    settings = settings,
                    chatMode = conv?.mode ?: extra.third,
                    draftText = uiFlags[1] as String,
                    pendingAttachments = attachments,
                    isStreaming = uiFlags[3] as Boolean,
                    isListeningVoice = uiFlags[4] as Boolean,
                    inlineError = extra.first,
                    revealedActionsMessageId = extra.second
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    val allConversations: StateFlow<List<Conversation>> = chatRepo.conversations

    fun openConversation(conversationId: String) {
        stopStreaming()
        _inlineError.value = null
        _revealedActionsId.value = null
        viewModelScope.launch {
            _isLoadingConversation.value = true
            val result = chatRepo.loadConversation(conversationId)
            _chatMode.value = result.getOrNull()?.mode ?: ChatMode.CHAT
            chatRepo.selectConversation(conversationId)
            _isLoadingConversation.value = false
        }
    }

    fun startNewChat(mode: ChatMode = ChatMode.CHAT) {
        stopStreaming()
        _inlineError.value = null
        _revealedActionsId.value = null
        _pendingAttachments.value = emptyList()
        _chatMode.value = mode
        chatRepo.startNewBlankChat()
    }

    fun setChatMode(mode: ChatMode) {
        _chatMode.value = mode
    }

    fun onDraftChange(text: String) {
        _draftText.value = text
    }

    fun addMockAttachment(type: String) {
        val label = when (type) {
            "Camera" -> "IMG_2026_notes.jpg"
            "Gallery" -> "diagram_sketch.png"
            else -> "assignment_brief.pdf"
        }
        val item = ChatAttachment(
            id = "att_${System.currentTimeMillis()}",
            label = label,
            type = type
        )
        _pendingAttachments.update { it + item }
    }

    fun removeAttachment(id: String) {
        _pendingAttachments.update { list -> list.filterNot { it.id == id } }
    }

    fun startVoiceListening() {
        _isListeningVoice.value = true
    }

    fun cancelVoiceListening() {
        _isListeningVoice.value = false
    }

    fun finishVoiceListening() {
        _isListeningVoice.value = false
        val current = _draftText.value
        val transcript = when (_chatMode.value) {
            ChatMode.IMAGE -> "Minimal glass workspace at golden hour with soft natural shadows"
            ChatMode.VIDEO -> "Slow aerial drone shot drifting over misty hills at sunrise"
            ChatMode.CHAT -> "How should I manage UI state cleanly with StateFlow in Jetpack Compose?"
        }
        _draftText.value = if (current.isBlank()) transcript else "$current $transcript"
    }

    fun toggleMessageActions(messageId: String) {
        _revealedActionsId.update { if (it == messageId) null else messageId }
    }

    fun togglePinConversation(conversationId: String) {
        chatRepo.togglePin(conversationId)
    }

    fun selectModel(modelId: String) {
        modelRepo.selectModel(modelId)
    }

    fun sendPrompt(overridePrompt: String? = null) {
        val raw = (overridePrompt ?: _draftText.value).trim()
        val attachments = _pendingAttachments.value
        if (raw.isEmpty() && attachments.isEmpty()) return
        if (_isStreaming.value) return

        val prompt = raw.ifEmpty { "Please explain this attached file." }
        _draftText.value = ""
        _pendingAttachments.value = emptyList()
        _inlineError.value = null
        _lastFailedPrompt.value = null

        val currentData = (uiState.value as? UiState.Success)?.data
        val selectedModel = currentData?.selectedModel ?: modelRepo.autoModel
        val settings = currentData?.settings ?: AppSettings()
        val mode = currentData?.chatMode ?: _chatMode.value

        val (convId, _) = chatRepo.ensureConversationForPrompt(
            prompt = prompt,
            modelId = selectedModel.id,
            modelLabel = selectedModel.shortName,
            attachments = attachments,
            mode = mode
        )

        if (settings.offlineDemoMode) {
            _lastFailedPrompt.value = prompt
            _inlineError.value = "You're in offline demo mode — go online and tap Retry."
            return
        }

        dispatchReply(convId, prompt, mode, selectedModel.name, settings)
    }

    fun retryLastPrompt() {
        val prompt = _lastFailedPrompt.value ?: "Explain integration basics"
        _inlineError.value = null
        val currentData = (uiState.value as? UiState.Success)?.data
        val selectedModel = currentData?.selectedModel ?: modelRepo.autoModel
        val settings = currentData?.settings ?: AppSettings()
        val mode = currentData?.chatMode ?: _chatMode.value
        val convId = chatRepo.activeConversationId.value ?: return
        dispatchReply(convId, prompt, mode, selectedModel.name, settings)
    }

    private fun dispatchReply(
        convId: String,
        prompt: String,
        mode: ChatMode,
        modelName: String,
        settings: AppSettings
    ) {
        if (mode != ChatMode.CHAT) {
            _lastFailedPrompt.value = prompt
            _inlineError.value = "Image and video generation are not connected yet. Switch to Chat mode."
            return
        }
        val route = modelRepo.resolve(modelRepo.selectedModelId.value)
        if (route == null) {
            _lastFailedPrompt.value = prompt
            _inlineError.value = "No provider key yet. Open Settings, then AI providers, and add one free key."
            return
        }
        val msgId = "msg_a_${System.currentTimeMillis()}"
        chatRepo.appendMessage(
            convId,
            ChatMessage(
                id = msgId,
                role = MessageRole.ASSISTANT,
                content = "",
                timestampLabel = "Just now",
                dateGroup = "Today",
                modelName = route.apiModel,
                isStreaming = true
            )
        )
        streamReplyInto(convId, msgId, route, prompt)
    }

    private fun streamReplyInto(
        convId: String,
        msgId: String,
        route: com.bansify.ai.data.repo.ModelRoute,
        prompt: String
    ) {
        val turns = buildTurns(convId)
        val system = systemPrompt()
        streamingJob = viewModelScope.launch {
            _isStreaming.value = true
            val builder = StringBuilder()
            try {
                chatEngine.stream(
                    provider = route.provider,
                    model = route.apiModel,
                    apiKey = route.apiKey,
                    systemPrompt = system,
                    turns = turns,
                    shouldStop = { !isActive }
                ) { delta ->
                    builder.append(delta)
                    val snapshot = builder.toString()
                    chatRepo.updateMessage(convId, msgId, persist = false) {
                        it.copy(content = snapshot, isStreaming = true)
                    }
                }
                chatRepo.updateMessage(convId, msgId) { it.copy(isStreaming = false) }
            } catch (error: AiException) {
                failMessage(convId, msgId, builder.toString(), error.message ?: "Request failed.", prompt)
            } catch (error: Exception) {
                if (isActive) {
                    failMessage(
                        convId,
                        msgId,
                        builder.toString(),
                        "Could not reach ${route.provider.name}. ${error.message ?: ""}".trim(),
                        prompt
                    )
                }
            } finally {
                _isStreaming.value = false
            }
        }
    }

    private fun failMessage(
        convId: String,
        msgId: String,
        partial: String,
        message: String,
        prompt: String
    ) {
        chatRepo.updateMessage(convId, msgId) {
            it.copy(
                content = partial.ifBlank { message },
                isStreaming = false
            )
        }
        _inlineError.value = message
        _lastFailedPrompt.value = prompt
    }

    /** Conversation history in the shape providers expect, oldest first, last 20 turns. */
    private fun buildTurns(conversationId: String): List<AiTurn> {
        val conversation = chatRepo.conversations.value.firstOrNull { it.id == conversationId } ?: return emptyList()
        return conversation.messages
            .filter { it.generatedMedia == null && it.content.isNotBlank() }
            .takeLast(20)
            .map { AiTurn(isUser = it.role == MessageRole.USER, text = it.content) }
    }

    private fun systemPrompt(): String {
        val profile = (uiState.value as? UiState.Success)?.data?.userProfile
        val name = profile?.name?.takeIf { it.isNotBlank() && !profile.isGuest }.orEmpty()
        val profession = profile?.profession?.orEmpty().orEmpty()
        return buildString {
            append("You are Bansify AI, a concise and friendly assistant.")
            if (name.isNotEmpty()) append(" The user's name is $name.")
            if (profession.isNotEmpty()) append(" Their profession is $profession.")
            append(" Reply in the language the user writes in.")
        }
    }

    private fun streamMediaReply(
        conversationId: String,
        prompt: String,
        isVideo: Boolean
    ) {
        val seed = mediaSeedCounter++
        val media = GeneratedMedia(
            isVideo = isVideo,
            prompt = prompt,
            aspectRatio = if (isVideo) "16:9" else "1:1",
            durationLabel = if (isVideo) "0:08 · 1080p" else null,
            seedIndex = seed
        )
        val msgId = "msg_media_${System.currentTimeMillis()}"
        val caption = MockChats.generateMediaCaption(prompt, isVideo)
        val placeholder = ChatMessage(
            id = msgId,
            role = MessageRole.ASSISTANT,
            content = if (isVideo) "Generating video…" else "Generating image…",
            timestampLabel = "Just now",
            dateGroup = "Today",
            modelName = if (isVideo) "Bansify Video" else "Bansify Image",
            isStreaming = true,
            generatedMedia = media
        )
        chatRepo.appendMessage(conversationId, placeholder)

        streamingJob = viewModelScope.launch {
            _isStreaming.value = true
            delay(if (isVideo) 900L else 750L)
            if (isActive) {
                chatRepo.updateMessage(conversationId, msgId) {
                    it.copy(content = caption, isStreaming = false, generatedMedia = media)
                }
            }
            _isStreaming.value = false
        }
    }

    fun regenerateMessage(messageId: String) {
        val convId = chatRepo.activeConversationId.value ?: return
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        val messages = currentData.messages
        val targetIndex = messages.indexOfFirst { it.id == messageId }
        val userPrompt = if (targetIndex > 0) {
            messages.subList(0, targetIndex).lastOrNull { it.role == MessageRole.USER }?.content
        } else {
            messages.lastOrNull { it.role == MessageRole.USER }?.content
        } ?: "Explain integration basics"

        stopStreaming()
        val mode = currentData.chatMode
        if (mode != ChatMode.CHAT) {
            _inlineError.value = "Image and video generation are not connected yet."
            return
        }
        if (mode == ChatMode.IMAGE || mode == ChatMode.VIDEO) {
            val isVideo = mode == ChatMode.VIDEO
            val nextMedia = GeneratedMedia(
                isVideo = isVideo,
                prompt = userPrompt,
                aspectRatio = if (isVideo) "16:9" else "1:1",
                durationLabel = if (isVideo) "0:08 · 1080p" else null,
                seedIndex = mediaSeedCounter++
            )
            streamingJob = viewModelScope.launch {
                _isStreaming.value = true
                chatRepo.updateMessage(convId, messageId) {
                    it.copy(
                        content = if (isVideo) "Regenerating video…" else "Regenerating image…",
                        isStreaming = true,
                        generatedMedia = nextMedia
                    )
                }
                delay(if (isVideo) 850L else 700L)
                if (isActive) {
                    chatRepo.updateMessage(convId, messageId) {
                        it.copy(
                            content = MockChats.generateMediaCaption(userPrompt, isVideo),
                            isStreaming = false,
                            generatedMedia = nextMedia
                        )
                    }
                }
                _isStreaming.value = false
            }
            return
        }

        val route = modelRepo.resolve(modelRepo.selectedModelId.value)
        if (route == null) {
            _inlineError.value = "No provider key yet. Add one in Settings, then AI providers."
            return
        }
        chatRepo.updateMessage(convId, messageId) {
            it.copy(content = "", isStreaming = true, modelName = route.apiModel)
        }
        streamReplyInto(convId, messageId, route, userPrompt)
    }

    fun replaySeedStreamingDemo() {
        retryLastPrompt()
    }



    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        _isStreaming.value = false
        val convId = chatRepo.activeConversationId.value ?: return
        val conv = chatRepo.conversations.value.firstOrNull { it.id == convId } ?: return
        conv.messages.filter { it.isStreaming }.forEach { msg ->
            chatRepo.updateMessage(convId, msg.id) { it.copy(isStreaming = false) }
        }
    }

    fun setFeedback(messageId: String, feedback: MessageFeedback) {
        val convId = chatRepo.activeConversationId.value ?: return
        chatRepo.setMessageFeedback(convId, messageId, feedback)
    }

    fun renameCurrentConversation(newTitle: String) {
        val convId = chatRepo.activeConversationId.value ?: return
        chatRepo.renameConversation(convId, newTitle)
    }

    fun clearCurrentChat() {
        stopStreaming()
        _inlineError.value = null
        chatRepo.clearActiveConversationMessages()
    }

    fun triggerDemoError() {
        _lastFailedPrompt.value = "Explain integration basics"
        _inlineError.value = "Mock server timeout — tap Retry to test recovery."
    }

    fun toggleOfflineDemo() {
        viewModelScope.launch {
            val current = (uiState.value as? UiState.Success)?.data?.settings?.offlineDemoMode ?: false
            prefsRepo.setOfflineDemoMode(!current)
        }
    }

}
