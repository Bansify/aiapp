package com.bansify.ai.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.state.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onStartNewChat: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showOverflow by remember { mutableStateOf(false) }
    var actionTarget by remember { mutableStateOf<Conversation?>(null) }
    var renameTarget by remember { mutableStateOf<Conversation?>(null) }
    var renameDraft by remember { mutableStateOf("") }

    val successData = (state as? UiState.Success)?.data
    val multiSelectActive = successData?.selectedIds?.isNotEmpty() == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (multiSelectActive && successData != null) {
                BansifyTopBar(
                    title = "${successData.selectedIds.size} selected",
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cancel selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::pinSelected, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.PushPin, contentDescription = "Pin selected")
                        }
                        IconButton(
                            onClick = {
                                viewModel.clearSelection()
                                scope.launch { snackbarHostState.showSnackbar("Demo: Exported selected chats") }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.FileDownload, contentDescription = "Export selected")
                        }
                        IconButton(onClick = viewModel::deleteSelected, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete selected",
                                tint = BansifyThemeTokens.extendedColors.danger
                            )
                        }
                    }
                )
            } else {
                BansifyTopBar(
                    title = "History",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "History options")
                        }
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Refresh (Loading demo)", style = BansifyTextStyles.Body) },
                                onClick = {
                                    showOverflow = false
                                    viewModel.refresh(simulateError = false)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                                onClick = {
                                    showOverflow = false
                                    viewModel.refresh(simulateError = true)
                                }
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("history_screen")
        ) {
            when (val current = state) {
                is UiState.Loading -> SkeletonList(count = 5)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load history",
                    message = current.message,
                    onRetry = { viewModel.refresh(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "No chats yet",
                    subtitle = "Your conversations will appear here cleanly organized.",
                    actionLabel = "Start a new chat",
                    onActionClick = onStartNewChat
                )
                is UiState.Success -> {
                    val data = current.data
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.xs)
                    ) {
                        TextFieldOutlined(
                            value = data.query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = "Search history…",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            testTag = "history_search_input"
                        )
                        Spacer(Modifier.height(Space.xs))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Space.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HistoryFilter.entries.forEach { filter ->
                                val selected = data.filter == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.onFilterChange(filter) },
                                    label = { Text(filter.label, style = BansifyTextStyles.CaptionStrong) },
                                    shape = BansifyRadius.Chip,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("history_filter_${filter.name.lowercase()}")
                                )
                            }
                        }
                    }

                    if (data.conversations.isEmpty()) {
                        EmptyState(
                            title = "No matching chats",
                            subtitle = "Try clearing your search or filters, or start a new chat.",
                            actionLabel = "Start a new chat",
                            onActionClick = onStartNewChat
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Space.md),
                            verticalArrangement = Arrangement.spacedBy(Space.xs),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(data.conversations, key = { it.id }) { conv ->
                                val isSelected = conv.id in data.selectedIds
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { target ->
                                        when (target) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                viewModel.deleteConversation(conv.id)
                                                scope.launch {
                                                    val res = snackbarHostState.showSnackbar(
                                                        message = "\"${conv.title}\" deleted",
                                                        actionLabel = "Undo",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (res == SnackbarResult.ActionPerformed) {
                                                        viewModel.undoDelete()
                                                    }
                                                }
                                                true
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                viewModel.togglePin(conv.id)
                                                false
                                            }
                                            SwipeToDismissBoxValue.Settled -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val isDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                                        val bg = if (isDelete) {
                                            BansifyThemeTokens.extendedColors.dangerContainer
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        }
                                        Box(
                                            contentAlignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(BansifyRadius.Card)
                                                .background(bg)
                                                .padding(horizontal = Space.md)
                                        ) {
                                            Icon(
                                                imageVector = if (isDelete) Icons.Rounded.DeleteOutline else Icons.Rounded.PushPin,
                                                contentDescription = if (isDelete) "Delete" else "Pin",
                                                tint = if (isDelete) {
                                                    BansifyThemeTokens.extendedColors.danger
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }
                                ) {
                                    Surface(
                                        shape = BansifyRadius.Card,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        ),
                                        tonalElevation = 0.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(BansifyRadius.Card)
                                            .combinedClickable(
                                                onClick = {
                                                    if (multiSelectActive) {
                                                        viewModel.toggleSelection(conv.id)
                                                    } else {
                                                        onOpenConversation(conv.id)
                                                    }
                                                },
                                                onLongClick = { viewModel.toggleSelection(conv.id) }
                                            )
                                            .testTag("history_row_${conv.id}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(Space.md)
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(Space.sm))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (conv.isPinned) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.PushPin,
                                                            contentDescription = "Pinned",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(Modifier.width(Space.xxs))
                                                    }
                                                    Text(
                                                        text = conv.title,
                                                        style = BansifyTextStyles.BodyStrong,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer(Modifier.width(Space.xs))
                                                    Text(
                                                        text = conv.relativeTime,
                                                        style = BansifyTextStyles.Caption,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(Modifier.height(Space.xxs))
                                                Text(
                                                    text = conv.preview,
                                                    style = BansifyTextStyles.Caption,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(Space.xs))
                                                Surface(
                                                    shape = BansifyRadius.Chip,
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    tonalElevation = 0.dp
                                                ) {
                                                    Text(
                                                        text = conv.modelLabel,
                                                        style = BansifyTextStyles.Caption,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { actionTarget = conv },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.MoreVert,
                                                    contentDescription = "Conversation actions",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    actionTarget?.let { conv ->
        SheetScaffold(
            onDismiss = { actionTarget = null },
            title = conv.title
        ) {
            HistorySheetOption(Icons.Rounded.Edit, "Rename") {
                renameDraft = conv.title
                renameTarget = conv
                actionTarget = null
            }
            HistorySheetOption(Icons.Rounded.PushPin, if (conv.isPinned) "Unpin" else "Pin") {
                viewModel.togglePin(conv.id)
                actionTarget = null
            }
            HistorySheetOption(Icons.Rounded.ContentCopy, "Duplicate") {
                viewModel.duplicateConversation(conv.id)
                actionTarget = null
            }
            HistorySheetOption(Icons.Rounded.DeleteOutline, "Delete") {
                viewModel.deleteConversation(conv.id)
                actionTarget = null
                scope.launch {
                    val res = snackbarHostState.showSnackbar("Deleted \"${conv.title}\"", actionLabel = "Undo")
                    if (res == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                }
            }
        }
    }

    renameTarget?.let { conv ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = BansifyRadius.SheetFull,
            title = { Text("Rename chat", style = BansifyTextStyles.H2) },
            text = {
                TextFieldOutlined(
                    value = renameDraft,
                    onValueChange = { renameDraft = it.take(48) },
                    placeholder = "Title"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameConversation(conv.id, renameDraft)
                        renameTarget = null
                    }
                ) {
                    Text("Save", style = BansifyTextStyles.BodyStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text("Cancel", style = BansifyTextStyles.BodyStrong)
                }
            }
        )
    }
}

@Composable
private fun HistorySheetOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(BansifyRadius.Input)
            .combinedClickable(onClick = onClick)
            .padding(horizontal = Space.sm, vertical = Space.sm)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Space.sm))
        Text(text = label, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
    }
}
