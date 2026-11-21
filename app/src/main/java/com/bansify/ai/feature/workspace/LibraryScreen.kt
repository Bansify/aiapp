package com.bansify.ai.feature.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class LibraryItem(
    val id: String,
    val title: String,
    val category: String, // "Prompts", "Images", "Videos", "Docs"
    val preview: String,
    val savedAt: String
)

private val initialLibraryItems = listOf(
    LibraryItem(
        id = "lib_1",
        title = "Android Compose PR Review Prompt",
        category = "Prompts",
        preview = "Review this Compose screen for state hoisting, stability, accessibility touch targets, and recomposition traps.",
        savedAt = "Today"
    ),
    LibraryItem(
        id = "lib_2",
        title = "Minimalist Workspace Hero Banner",
        category = "Images",
        preview = "16:9 · Clean architectural studio desk at dawn with soft cobalt accent light.",
        savedAt = "Yesterday"
    ),
    LibraryItem(
        id = "lib_3",
        title = "Product Launch Teaser Clip",
        category = "Videos",
        preview = "10s · Smooth 60fps abstract glass motion loop for app splash showcase.",
        savedAt = "2 days ago"
    ),
    LibraryItem(
        id = "lib_4",
        title = "Calculus Integration Cheat Sheet",
        category = "Docs",
        preview = "Power rule, substitution, ILATE integration by parts, and Python trapezoidal verification.",
        savedAt = "4 days ago"
    )
)

@Composable
fun LibraryScreen(
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf(initialLibraryItems) }
    var state by remember { mutableStateOf<UiState<Unit>>(UiState.Loading) }
    var query by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    var showOverflow by remember { mutableStateOf(false) }

    fun load(simulateError: Boolean = false) {
        scope.launch {
            state = UiState.Loading
            delay(620L)
            state = when {
                simulateError -> UiState.Error("Unable to load Library items (mock error).")
                items.isEmpty() -> UiState.Empty
                else -> UiState.Success(Unit)
            }
        }
    }

    LaunchedEffect(Unit) { load(false) }

    val filters = listOf("All", "Prompts", "Images", "Videos", "Docs")
    val filtered = remember(items, query, activeFilter) {
        val q = query.trim().lowercase()
        items.filter { item ->
            val matchesCat = activeFilter == "All" || item.category == activeFilter
            val matchesQ = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.preview.lowercase().contains(q)
            matchesCat && matchesQ
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BansifyTopBar(
                title = "Library",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Library options")
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
                                load(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                load(true)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("library_screen")
        ) {
            when (val current = state) {
                is UiState.Loading -> SkeletonList(count = 4)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load Library",
                    message = current.message,
                    onRetry = { load(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "Your Library is empty",
                    subtitle = "Saved prompts, images, videos, and documents will appear here."
                )
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.xs)
                    ) {
                        TextFieldOutlined(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = "Search library…",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            testTag = "library_search"
                        )
                        Spacer(Modifier.height(Space.xs))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Space.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filters.take(4).forEach { cat ->
                                FilterChip(
                                    selected = activeFilter == cat,
                                    onClick = { activeFilter = cat },
                                    label = { Text(cat, style = BansifyTextStyles.CaptionStrong) },
                                    shape = BansifyRadius.Chip,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    if (filtered.isEmpty()) {
                        EmptyState(
                            title = "No matching items",
                            subtitle = "Clear your filter or search query to see everything saved.",
                            actionLabel = "Reset filter",
                            onActionClick = {
                                query = ""
                                activeFilter = "All"
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Space.md),
                            verticalArrangement = Arrangement.spacedBy(Space.sm),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                Surface(
                                    shape = BansifyRadius.Card,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(Space.md)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = when (item.category) {
                                                    "Images" -> Icons.Outlined.Image
                                                    "Videos" -> Icons.Outlined.Videocam
                                                    "Docs" -> Icons.Outlined.Description
                                                    else -> Icons.Rounded.BookmarkBorder
                                                },
                                                contentDescription = item.category,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(Space.xs))
                                            Text(
                                                text = item.category,
                                                style = BansifyTextStyles.CaptionStrong,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = item.savedAt,
                                                style = BansifyTextStyles.Caption,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(Space.xs))
                                        Text(
                                            text = item.title,
                                            style = BansifyTextStyles.BodyStrong,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(Space.xxs))
                                        Text(
                                            text = item.preview,
                                            style = BansifyTextStyles.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(Space.xs))
                                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                            IconButton(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(item.preview))
                                                    scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.ContentCopy,
                                                    contentDescription = "Copy item",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    items = items.filterNot { it.id == item.id }
                                                    if (items.isEmpty()) state = UiState.Empty
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.DeleteOutline,
                                                    contentDescription = "Delete item",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
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
}
