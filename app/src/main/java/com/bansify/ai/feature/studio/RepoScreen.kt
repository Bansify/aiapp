package com.bansify.ai.feature.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.mock.ProviderGroup
import com.bansify.ai.feature.chat.AddToChatSheet
import com.bansify.ai.feature.chat.ChatComposer
import com.bansify.ai.feature.chat.MessageItem
import com.bansify.ai.feature.chat.ModelSheet
import com.bansify.ai.state.UiState
import kotlinx.coroutines.launch

private enum class StudioWorkspaceTab(val label: String, val icon: ImageVector) {
    CHAT("Chat", Icons.Outlined.ChatBubbleOutline),
    PREVIEW("Preview", Icons.Outlined.PlayCircleOutline),
    CODE("Code", Icons.Outlined.Code)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoScreen(
    repoId: String,
    viewModel: StudioViewModel,
    providers: List<ProviderGroup>,
    autoModel: ModelItem,
    selectedModel: ModelItem,
    onSelectModel: (String) -> Unit,
    onNavigateModels: () -> Unit,
    onBack: () -> Unit,
    onOpenFile: (String) -> Unit
) {
    val allRepos by viewModel.repos.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isProjectStreaming.collectAsStateWithLifecycle()
    val liveRepo = allRepos.firstOrNull { it.id == repoId }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val chatListState = rememberLazyListState()

    var loadState by remember { mutableStateOf<UiState<MockRepo>>(UiState.Loading) }
    var activeTab by remember { mutableStateOf(StudioWorkspaceTab.CHAT) }
    var draftText by remember { mutableStateOf("") }
    var revealedActionsId by remember { mutableStateOf<String?>(null) }
    var showModelSheet by remember { mutableStateOf(false) }
    var showAddToChatSheet by remember { mutableStateOf(false) }
    var webSearchEnabled by remember { mutableStateOf(true) }
    var memoryEnabled by remember { mutableStateOf(true) }
    var showBranchSheet by remember { mutableStateOf(false) }
    var showIntegrationsSheet by remember { mutableStateOf(false) }
    var showZipSheet by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    fun fetch(simulateError: Boolean = false) {
        scope.launch {
            loadState = UiState.Loading
            val res = viewModel.loadRepo(repoId, simulateError)
            loadState = res.fold(
                onSuccess = { repo -> if (repo == null) UiState.Empty else UiState.Success(repo) },
                onFailure = { UiState.Error(it.message ?: "Unable to load project") }
            )
        }
    }

    LaunchedEffect(repoId) { fetch(false) }

    val repo = liveRepo ?: (loadState as? UiState.Success)?.data
    val messageCount = repo?.chatMessages?.size ?: 0
    LaunchedEffect(messageCount) {
        if (messageCount > 0) {
            chatListState.scrollToItem(messageCount - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                BansifyTopBar(
                    title = repo?.projectName ?: "Studio Project",
                    onBack = onBack,
                    actions = {
                        IconButton(
                            onClick = { showIntegrationsSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("studio_connections_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Hub,
                                contentDescription = "Manage project connections",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Project options")
                        }
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Connections (29+ tools)", style = BansifyTextStyles.Body) },
                                leadingIcon = { Icon(Icons.Rounded.Hub, contentDescription = null) },
                                onClick = {
                                    showOverflow = false
                                    showIntegrationsSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch Git branch", style = BansifyTextStyles.Body) },
                                leadingIcon = { Icon(Icons.Rounded.AccountTree, contentDescription = null) },
                                onClick = {
                                    showOverflow = false
                                    showBranchSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Upload ZIP archive", style = BansifyTextStyles.Body) },
                                leadingIcon = { Icon(Icons.Rounded.FolderZip, contentDescription = null) },
                                onClick = {
                                    showOverflow = false
                                    showZipSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Refresh (Loading demo)", style = BansifyTextStyles.Body) },
                                onClick = {
                                    showOverflow = false
                                    fetch(false)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                                onClick = {
                                    showOverflow = false
                                    fetch(true)
                                }
                            )
                        }
                    }
                )

                // Header mode switcher: Chat | Preview | Code
                if (repo != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.xs)
                    ) {
                        StudioWorkspaceTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            Surface(
                                shape = BansifyRadius.Full,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .clip(BansifyRadius.Full)
                                    .clickable(role = Role.Tab) { activeTab = tab }
                                    .testTag("studio_mode_${tab.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(Space.xxs))
                                    Text(
                                        text = tab.label,
                                        style = BansifyTextStyles.CaptionStrong,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (repo != null && activeTab == StudioWorkspaceTab.CHAT) {
                ChatComposer(
                    text = draftText,
                    attachments = emptyList(),
                    chatMode = ChatMode.CHAT,
                    selectedModelLabel = selectedModel.shortName,
                    isStreaming = isStreaming,
                    isListening = false,
                    sendOnEnter = true,
                    onTextChange = { draftText = it },
                    onRemoveAttachment = {},
                    onResetChatMode = {},
                    onModelClick = { showModelSheet = true },
                    onAttachClick = { showAddToChatSheet = true },
                    onMicClick = {
                        draftText = "Add a responsive hero banner and connect Cloudflare deploy preview"
                    },
                    onCancelListening = {},
                    onFinishListening = {},
                    onSendClick = {
                        val prompt = draftText
                        draftText = ""
                        viewModel.sendProjectPrompt(repo.id, prompt)
                    },
                    onStopClick = { viewModel.stopProjectStreaming(repo.id) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("studio_repo_screen")
        ) {
            when (val current = loadState) {
                is UiState.Loading -> SkeletonList(count = 5)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load project",
                    message = current.message,
                    onRetry = { fetch(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "Project not found",
                    subtitle = "Return to Studio and open or create a project.",
                    actionLabel = "Go back",
                    onActionClick = onBack
                )
                is UiState.Success -> {
                    val activeRepo = repo ?: current.data
                    when (activeTab) {
                        StudioWorkspaceTab.CHAT -> {
                            ProjectConnectionsBar(
                                repo = activeRepo,
                                onOpenBranch = { showBranchSheet = true },
                                onOpenConnections = { showIntegrationsSheet = true }
                            )
                            LazyColumn(
                                state = chatListState,
                                contentPadding = PaddingValues(Space.md),
                                verticalArrangement = Arrangement.spacedBy(Space.lg),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .testTag("studio_chat_list")
                            ) {
                                items(activeRepo.chatMessages, key = { it.id }) { msg ->
                                    MessageItem(
                                        message = msg,
                                        showActions = revealedActionsId == msg.id,
                                        onTapAssistant = {
                                            revealedActionsId = if (revealedActionsId == msg.id) null else msg.id
                                        },
                                        onLongPress = {
                                            clipboard.setText(AnnotatedString(msg.content))
                                            scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                        },
                                        onCopy = {
                                            clipboard.setText(AnnotatedString(msg.content))
                                            scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                        },
                                        onRegenerate = {
                                            viewModel.sendProjectPrompt(activeRepo.id, "Refine the previous patch cleanly")
                                        },
                                        onFeedback = {
                                            scope.launch { snackbarHostState.showSnackbar("Feedback noted") }
                                        },
                                        onShare = {
                                            scope.launch { snackbarHostState.showSnackbar("Demo: Preview link copied") }
                                        },
                                        onCodeCopied = {
                                            scope.launch { snackbarHostState.showSnackbar("Code copied") }
                                        },
                                        onOpenMediaPreview = {}
                                    )
                                }
                            }
                        }

                        StudioWorkspaceTab.PREVIEW -> {
                            StudioPreviewPane(
                                repo = activeRepo,
                                onBackToChat = { activeTab = StudioWorkspaceTab.CHAT },
                                onOpenConnections = { showIntegrationsSheet = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        StudioWorkspaceTab.CODE -> {
                            StudioCodePane(
                                repo = activeRepo,
                                onOpenBranchPicker = { showBranchSheet = true },
                                onSelectBranch = { branch -> viewModel.switchBranch(activeRepo.id, branch) },
                                onOpenFile = onOpenFile,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showIntegrationsSheet && repo != null) {
        StudioIntegrationsSheet(
            projectName = repo.projectName,
            connectedIds = repo.connectedIntegrationIds,
            onToggleIntegration = { integrationId ->
                viewModel.toggleIntegration(repo.id, integrationId)
            },
            onDismiss = { showIntegrationsSheet = false }
        )
    }

    if (showBranchSheet && repo != null) {
        SheetScaffold(onDismiss = { showBranchSheet = false }, title = "Switch branch") {
            repo.branches.forEach { branch ->
                val selected = branch == repo.activeBranch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(BansifyRadius.Input)
                        .clickable {
                            viewModel.switchBranch(repo.id, branch)
                            showBranchSheet = false
                        }
                        .padding(horizontal = Space.sm, vertical = Space.xs)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountTree,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = branch,
                        style = BansifyTextStyles.BodyStrong,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Active branch",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showZipSheet) {
        ZipUploadSheet(onDismiss = { showZipSheet = false })
    }

    if (showModelSheet) {
        ModelSheet(
            providers = providers,
            autoModel = autoModel,
            selectedModelId = selectedModel.id,
            onSelectModel = onSelectModel,
            onSeeAllModels = onNavigateModels,
            onDismiss = { showModelSheet = false }
        )
    }

    if (showAddToChatSheet && repo != null) {
        AddToChatSheet(
            activeProjectName = repo.projectName,
            webSearchEnabled = webSearchEnabled,
            memoryEnabled = memoryEnabled,
            onPickCamera = {
                showAddToChatSheet = false
                scope.launch { snackbarHostState.showSnackbar("Attached screenshot to ${repo.projectName}") }
            },
            onPickPhotos = {
                showAddToChatSheet = false
                scope.launch { snackbarHostState.showSnackbar("Attached design reference photo") }
            },
            onPickFiles = {
                showAddToChatSheet = false
                showZipSheet = true
            },
            onRecordVideo = {
                showAddToChatSheet = false
                scope.launch { snackbarHostState.showSnackbar("Attached UI walkthrough clip (Demo)") }
            },
            onChangeProject = {
                showAddToChatSheet = false
                onBack()
            },
            onToggleWebSearch = { webSearchEnabled = it },
            onOpenConnectors = {
                showAddToChatSheet = false
                showIntegrationsSheet = true
            },
            onToggleMemory = { memoryEnabled = it },
            onDismiss = { showAddToChatSheet = false }
        )
    }
}
