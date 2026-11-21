package com.bansify.ai.feature.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.data.mock.UserProfile
import kotlinx.coroutines.delay

private enum class SidebarHistoryPill(val label: String) {
    ALL("All"),
    IMAGES("Images"),
    VIDEOS("Videos"),
    PROJECTS("Projects")
}

@Composable
fun DrawerContent(
    userProfile: UserProfile,
    conversations: List<Conversation>,
    studioProjects: List<MockRepo>,
    activeConversationId: String?,
    activeChatMode: ChatMode,
    onClose: () -> Unit,
    onNewChat: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onOpenProject: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onSeeAllHistory: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenImages: () -> Unit,
    onOpenVideos: () -> Unit,
    onOpenScheduled: () -> Unit,
    onOpenCustomise: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenModels: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var rawQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedPill by remember { mutableStateOf(SidebarHistoryPill.ALL) }
    var pinnedOnly by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(rawQuery) {
        delay(200L)
        debouncedQuery = rawQuery.trim().lowercase()
    }

    val filteredConversations = remember(conversations, debouncedQuery, pinnedOnly, selectedPill) {
        conversations
            .filter { conv -> !pinnedOnly || conv.isPinned }
            .filter { conv ->
                when (selectedPill) {
                    SidebarHistoryPill.ALL -> true
                    SidebarHistoryPill.IMAGES -> conv.mode == ChatMode.IMAGE
                    SidebarHistoryPill.VIDEOS -> conv.mode == ChatMode.VIDEO
                    SidebarHistoryPill.PROJECTS -> false
                }
            }
            .filter { conv ->
                debouncedQuery.isEmpty() ||
                    conv.title.lowercase().contains(debouncedQuery) ||
                    conv.preview.lowercase().contains(debouncedQuery)
            }
            .sortedByDescending { it.isPinned }
    }

    val filteredProjects = remember(studioProjects, debouncedQuery) {
        studioProjects.filter { proj ->
            debouncedQuery.isEmpty() ||
                proj.projectName.lowercase().contains(debouncedQuery) ||
                proj.fullName.lowercase().contains(debouncedQuery)
        }
    }

    val displayName = if (userProfile.isGuest || userProfile.name.isBlank()) {
        "Guest"
    } else {
        userProfile.name
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxSize()
            .testTag("navigation_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = Space.md)
        ) {
            // Top Header: Brand name left, Search + Close icons right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xs)
            ) {
                Text(
                    text = "Bansify AI",
                    style = BansifyTextStyles.H1,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Space.xs)
                )
                IconButton(
                    onClick = {
                        searchExpanded = !searchExpanded
                        if (!searchExpanded) rawQuery = ""
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("drawer_search_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search conversations",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("drawer_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close sidebar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            AnimatedVisibility(visible = searchExpanded) {
                Column {
                    TextFieldOutlined(
                        value = rawQuery,
                        onValueChange = { rawQuery = it },
                        placeholder = "Search chats & projects…",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (rawQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { rawQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                }
                            }
                        } else {
                            null
                        },
                        testTag = "drawer_search"
                    )
                    Spacer(Modifier.height(Space.sm))
                }
            }

            // Scrollable 4x2 option grid + filter pills + full-width history rows
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item(key = "workspace_grid") {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs), modifier = Modifier.fillMaxWidth()) {
                            SidebarGridTile(
                                icon = Icons.Outlined.Edit,
                                label = "New chat",
                                tag = "drawer_new_chat",
                                highlighted = activeConversationId == null && activeChatMode == ChatMode.CHAT,
                                onClick = onNewChat,
                                modifier = Modifier.weight(1f)
                            )
                            SidebarGridTile(
                                icon = Icons.Outlined.GridView,
                                label = "Library",
                                tag = "drawer_nav_library",
                                onClick = onOpenLibrary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs), modifier = Modifier.fillMaxWidth()) {
                            SidebarGridTile(
                                icon = Icons.Outlined.Image,
                                label = "Images",
                                tag = "drawer_nav_images",
                                highlighted = activeChatMode == ChatMode.IMAGE,
                                onClick = onOpenImages,
                                modifier = Modifier.weight(1f)
                            )
                            SidebarGridTile(
                                icon = Icons.Outlined.Videocam,
                                label = "Videos",
                                tag = "drawer_nav_videos",
                                highlighted = activeChatMode == ChatMode.VIDEO,
                                onClick = onOpenVideos,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs), modifier = Modifier.fillMaxWidth()) {
                            SidebarGridTile(
                                icon = Icons.Outlined.Schedule,
                                label = "Scheduled",
                                tag = "drawer_nav_scheduled",
                                onClick = onOpenScheduled,
                                modifier = Modifier.weight(1f)
                            )
                            SidebarGridTile(
                                icon = Icons.Outlined.Tune,
                                label = "Customise",
                                tag = "drawer_nav_customise",
                                onClick = onOpenCustomise,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs), modifier = Modifier.fillMaxWidth()) {
                            SidebarGridTile(
                                icon = Icons.Outlined.Code,
                                label = "Studio",
                                tag = "drawer_nav_studio",
                                onClick = onOpenStudio,
                                modifier = Modifier.weight(1f)
                            )
                            SidebarGridTile(
                                icon = Icons.Outlined.Layers,
                                label = "Models",
                                tag = "drawer_nav_models",
                                onClick = onOpenModels,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Filter Pills Row: All | Images | Videos | Projects + Filter Icon
                item(key = "conversations_header") {
                    Spacer(Modifier.height(Space.md))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Space.xs),
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            SidebarHistoryPill.entries.forEach { pill ->
                                val selected = selectedPill == pill
                                Surface(
                                    shape = BansifyRadius.Full,
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier
                                        .heightIn(min = 36.dp)
                                        .clip(BansifyRadius.Full)
                                        .clickable(role = Role.Tab) { selectedPill = pill }
                                        .testTag("drawer_pill_${pill.name.lowercase()}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs)
                                    ) {
                                        Text(
                                            text = pill.label,
                                            style = BansifyTextStyles.CaptionStrong,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showFilterMenu = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("drawer_filter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterAlt,
                                    contentDescription = "Filter conversations",
                                    tint = if (pinnedOnly) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All items", style = BansifyTextStyles.Body) },
                                    onClick = {
                                        pinnedOnly = false
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Pinned only", style = BansifyTextStyles.Body) },
                                    onClick = {
                                        pinnedOnly = true
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Open full history", style = BansifyTextStyles.Body) },
                                    onClick = {
                                        showFilterMenu = false
                                        onSeeAllHistory()
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Space.xs))
                }

                if (selectedPill == SidebarHistoryPill.PROJECTS) {
                    if (filteredProjects.isEmpty()) {
                        item(key = "drawer_projects_empty") {
                            Text(
                                text = "No projects yet. Open Studio to create one.",
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.md)
                            )
                        }
                    } else {
                        items(filteredProjects, key = { it.id }) { project ->
                            SidebarProjectRow(
                                project = project,
                                onClick = { onOpenProject(project.id) }
                            )
                        }
                    }
                } else if (filteredConversations.isEmpty()) {
                    item(key = "drawer_empty") {
                        Text(
                            text = when {
                                pinnedOnly -> "No pinned conversations yet."
                                selectedPill == SidebarHistoryPill.IMAGES -> "No image chats yet."
                                selectedPill == SidebarHistoryPill.VIDEOS -> "No video chats yet."
                                else -> "No conversations found."
                            },
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.md)
                        )
                    }
                } else {
                    items(filteredConversations, key = { it.id }) { conv ->
                        SidebarConversationRow(
                            conv = conv,
                            isSelected = conv.id == activeConversationId,
                            onClick = { onSelectConversation(conv.id) },
                            onTogglePin = { onTogglePin(conv.id) }
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(Space.md))
                }
            }

            // Bottom pinned profile & settings bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(BansifyRadius.Input)
                        .clickable(role = Role.Button, onClick = onOpenProfile)
                        .padding(horizontal = Space.xs, vertical = Space.xxs)
                        .testTag("drawer_header")
                ) {
                    BansifyBrandAvatar(size = 40.dp, initial = displayName)
                    Spacer(Modifier.width(Space.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = BansifyTextStyles.BodyStrong,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (userProfile.isGuest) "GUEST" else "FREE",
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("drawer_nav_settings")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
