package com.bansify.ai.feature.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Motion
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SkeletonChatMessages
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.GeneratedMedia
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.mock.ProviderGroup
import com.bansify.ai.data.mock.UserProfile
import com.bansify.ai.feature.drawer.DrawerContent
import com.bansify.ai.feature.studio.StudioIntegrationsSheet
import com.bansify.ai.state.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    providers: List<ProviderGroup>,
    autoModel: ModelItem,
    studioProjects: List<MockRepo>,
    onNavigateHistory: () -> Unit,
    onNavigateLibrary: () -> Unit,
    onNavigateScheduled: () -> Unit,
    onNavigateCustomise: () -> Unit,
    onNavigateModels: () -> Unit,
    onNavigateStudio: () -> Unit,
    onNavigateProject: (String) -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val conversations by viewModel.allConversations.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var isSidebarOpen by remember { mutableStateOf(false) }
    var showModelSheet by remember { mutableStateOf(false) }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showProjectPicker by remember { mutableStateOf(false) }
    var showConnectorsSheet by remember { mutableStateOf(false) }
    var webSearchEnabled by remember { mutableStateOf(true) }
    var memoryEnabled by remember { mutableStateOf(true) }
    var chatConnectedIds by remember { mutableStateOf(setOf("github", "cloudflare", "vercel", "figma")) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameDraft by remember { mutableStateOf("") }
    var longPressedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectTextMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var previewMedia by remember { mutableStateOf<GeneratedMedia?>(null) }

    val successData = (state as? UiState.Success)?.data
    val activeMode = successData?.chatMode ?: ChatMode.CHAT

    BackHandler(enabled = isSidebarOpen) {
        isSidebarOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                BansifyTopBar(
                    title = "",
                    navigationIcon = {
                        IconButton(
                            onClick = { isSidebarOpen = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("chat_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Open sidebar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    titleContent = {
                        val defaultTitle = when (activeMode) {
                            ChatMode.CHAT -> "New chat"
                            ChatMode.IMAGE -> "Images"
                            ChatMode.VIDEO -> "Videos"
                        }
                        val title = successData?.conversation?.title ?: defaultTitle
                        Text(
                            text = title,
                            style = BansifyTextStyles.H2,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(BansifyRadius.Chip)
                                .clickable(enabled = successData?.conversation != null) {
                                    renameDraft = title
                                    showRenameDialog = true
                                }
                                .padding(horizontal = Space.xs, vertical = Space.xs)
                                .testTag("chat_title_button")
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("chat_overflow_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        ChatOverflowMenu(
                            expanded = showOverflowMenu,
                            offlineDemoMode = successData?.settings?.offlineDemoMode == true,
                            onDismiss = { showOverflowMenu = false },
                            onReplayDemo = { viewModel.replaySeedStreamingDemo() },
                            onClearChat = { viewModel.clearCurrentChat() },
                            onExportMarkdown = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Demo: Markdown export preview ready")
                                }
                            },
                            onShareLink = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Coming soon: Public chat links")
                                }
                            },
                            onToggleOffline = { viewModel.toggleOfflineDemo() },
                            onSimulateError = { viewModel.triggerDemoError() }
                        )
                    }
                )
            },
            bottomBar = {
                if (successData != null) {
                    ChatComposer(
                        text = successData.draftText,
                        attachments = successData.pendingAttachments,
                        chatMode = successData.chatMode,
                        selectedModelLabel = successData.selectedModel.shortName,
                        isStreaming = successData.isStreaming,
                        isListening = successData.isListeningVoice,
                        sendOnEnter = successData.settings.sendOnEnter,
                        onTextChange = viewModel::onDraftChange,
                        onRemoveAttachment = viewModel::removeAttachment,
                        onResetChatMode = { viewModel.setChatMode(ChatMode.CHAT) },
                        onModelClick = { showModelSheet = true },
                        onAttachClick = { showAttachSheet = true },
                        onMicClick = viewModel::startVoiceListening,
                        onCancelListening = viewModel::cancelVoiceListening,
                        onFinishListening = viewModel::finishVoiceListening,
                        onSendClick = { viewModel.sendPrompt() },
                        onStopClick = viewModel::stopStreaming
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedVisibility(visible = successData?.settings?.offlineDemoMode == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Space.md, vertical = Space.xs)
                            .testTag("offline_banner")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WifiOff,
                            contentDescription = "Offline",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(Space.xs))
                        Text(
                            text = "You're offline — showing saved history",
                            style = BansifyTextStyles.CaptionStrong,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                when (val current = state) {
                    is UiState.Loading -> SkeletonChatMessages(modifier = Modifier.weight(1f))
                    is UiState.Error -> ErrorState(
                        title = "Couldn't load chat",
                        message = current.message,
                        onRetry = { viewModel.startNewChat() },
                        modifier = Modifier.weight(1f)
                    )
                    is UiState.Empty -> Unit
                    is UiState.Success -> {
                        MessageList(
                            messages = current.data.messages,
                            userProfile = current.data.userProfile,
                            chatMode = current.data.chatMode,
                            listState = listState,
                            revealedActionsId = current.data.revealedActionsMessageId,
                            inlineError = current.data.inlineError,
                            onRetryError = viewModel::retryLastPrompt,
                            onSuggestionClick = { prompt -> viewModel.sendPrompt(prompt) },
                            onTapAssistant = viewModel::toggleMessageActions,
                            onLongPressMessage = { longPressedMessage = it },
                            onCopyMessage = { msg ->
                                clipboard.setText(AnnotatedString(msg.content))
                                scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                            },
                            onRegenerateMessage = { msg -> viewModel.regenerateMessage(msg.id) },
                            onFeedback = { id, fb ->
                                viewModel.setFeedback(id, fb)
                                scope.launch {
                                    val label = if (fb == MessageFeedback.UP) "Feedback noted (Helpful)" else "Feedback noted"
                                    snackbarHostState.showSnackbar(label)
                                }
                            },
                            onShareMessage = {
                                scope.launch { snackbarHostState.showSnackbar("Demo: Share sheet preview") }
                            },
                            onCodeCopied = {
                                scope.launch { snackbarHostState.showSnackbar("Code copied") }
                            },
                            onOpenMediaPreview = { media -> previewMedia = media },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Full-screen sidebar overlay
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = slideInHorizontally(tween(Motion.Screen)) { -it },
            exit = slideOutHorizontally(tween(Motion.Screen)) { -it }
        ) {
            DrawerContent(
                userProfile = successData?.userProfile ?: UserProfile(),
                conversations = conversations,
                studioProjects = studioProjects,
                activeConversationId = successData?.conversation?.id,
                activeChatMode = activeMode,
                onClose = { isSidebarOpen = false },
                onNewChat = {
                    isSidebarOpen = false
                    viewModel.startNewChat(ChatMode.CHAT)
                },
                onSelectConversation = { id ->
                    isSidebarOpen = false
                    viewModel.openConversation(id)
                },
                onOpenProject = { projectId ->
                    isSidebarOpen = false
                    onNavigateProject(projectId)
                },
                onTogglePin = viewModel::togglePinConversation,
                onSeeAllHistory = {
                    isSidebarOpen = false
                    onNavigateHistory()
                },
                onOpenLibrary = {
                    isSidebarOpen = false
                    onNavigateLibrary()
                },
                onOpenImages = {
                    isSidebarOpen = false
                    viewModel.startNewChat(ChatMode.IMAGE)
                },
                onOpenVideos = {
                    isSidebarOpen = false
                    viewModel.startNewChat(ChatMode.VIDEO)
                },
                onOpenScheduled = {
                    isSidebarOpen = false
                    onNavigateScheduled()
                },
                onOpenCustomise = {
                    isSidebarOpen = false
                    onNavigateCustomise()
                },
                onOpenStudio = {
                    isSidebarOpen = false
                    onNavigateStudio()
                },
                onOpenModels = {
                    isSidebarOpen = false
                    onNavigateModels()
                },
                onOpenSettings = {
                    isSidebarOpen = false
                    onNavigateSettings()
                },
                onOpenProfile = {
                    isSidebarOpen = false
                    onNavigateProfile()
                }
            )
        }
    }

    if (showModelSheet && successData != null) {
        ModelSheet(
            providers = providers,
            autoModel = autoModel,
            selectedModelId = successData.selectedModel.id,
            onSelectModel = viewModel::selectModel,
            onSeeAllModels = onNavigateModels,
            onDismiss = { showModelSheet = false }
        )
    }

    if (showAttachSheet) {
        AddToChatSheet(
            activeProjectName = studioProjects.firstOrNull()?.projectName ?: "Select a Studio project",
            webSearchEnabled = webSearchEnabled,
            memoryEnabled = memoryEnabled,
            onPickCamera = {
                viewModel.addMockAttachment("Camera")
                showAttachSheet = false
            },
            onPickPhotos = {
                viewModel.addMockAttachment("Photos")
                showAttachSheet = false
            },
            onPickFiles = {
                viewModel.addMockAttachment("File")
                showAttachSheet = false
            },
            onRecordVideo = {
                showAttachSheet = false
                viewModel.startNewChat(ChatMode.VIDEO)
                scope.launch { snackbarHostState.showSnackbar("Switched to Video mode") }
            },
            onChangeProject = {
                showAttachSheet = false
                showProjectPicker = true
            },
            onToggleWebSearch = { webSearchEnabled = it },
            onOpenConnectors = {
                showAttachSheet = false
                showConnectorsSheet = true
            },
            onToggleMemory = { memoryEnabled = it },
            onDismiss = { showAttachSheet = false }
        )
    }

    if (showProjectPicker) {
        ProjectPickerSheet(
            projects = studioProjects,
            onSelectProject = { projectId ->
                showProjectPicker = false
                onNavigateProject(projectId)
            },
            onOpenStudio = {
                showProjectPicker = false
                onNavigateStudio()
            },
            onDismiss = { showProjectPicker = false }
        )
    }

    if (showConnectorsSheet) {
        StudioIntegrationsSheet(
            projectName = "Bansify Chat",
            connectedIds = chatConnectedIds,
            onToggleIntegration = { id ->
                chatConnectedIds = if (id in chatConnectedIds) chatConnectedIds - id else chatConnectedIds + id
            },
            onDismiss = { showConnectorsSheet = false }
        )
    }

    longPressedMessage?.let { msg ->
        MessageOptionsSheet(
            message = msg,
            onCopy = {
                clipboard.setText(AnnotatedString(msg.content))
                longPressedMessage = null
                scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
            },
            onShare = {
                longPressedMessage = null
                scope.launch { snackbarHostState.showSnackbar("Demo: Share preview") }
            },
            onSelectText = {
                selectTextMessage = msg
                longPressedMessage = null
            },
            onRegenerate = {
                viewModel.regenerateMessage(msg.id)
                longPressedMessage = null
            },
            onReport = {
                longPressedMessage = null
                scope.launch { snackbarHostState.showSnackbar("Demo: Report noted locally") }
            },
            onDismiss = { longPressedMessage = null }
        )
    }

    selectTextMessage?.let { msg ->
        SelectMessageTextDialog(message = msg, onDismiss = { selectTextMessage = null })
    }

    if (showRenameDialog) {
        RenameChatDialog(
            value = renameDraft,
            onValueChange = { renameDraft = it },
            onSave = {
                viewModel.renameCurrentConversation(renameDraft)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    previewMedia?.let { media ->
        MediaPreviewDialog(
            media = media,
            onCopyPrompt = {
                clipboard.setText(AnnotatedString(media.prompt))
                previewMedia = null
                scope.launch { snackbarHostState.showSnackbar("Prompt copied") }
            },
            onSaveDemo = {
                previewMedia = null
                scope.launch {
                    val kind = if (media.isVideo) "Video" else "Image"
                    snackbarHostState.showSnackbar("Demo: $kind saved to Library")
                }
            },
            onDismiss = { previewMedia = null }
        )
    }
}
