package com.bansify.ai.feature.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.IconPill
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.data.mock.MockIntegrations
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioHomeScreen(
    viewModel: StudioViewModel,
    onBack: () -> Unit,
    onOpenRepo: (String) -> Unit
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    var quickPrompt by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var showZipSheet by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BansifyTopBar(
                title = "Studio",
                onBack = onBack,
                actions = {
                    IconPill(
                        label = "New",
                        leadingIcon = Icons.Rounded.Add,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { showCreateSheet = true },
                        testTag = "studio_new_project_button"
                    )
                    IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Studio options")
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Upload ZIP archive (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                showZipSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh (Loading demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                viewModel.refreshHome(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                viewModel.refreshHome(true)
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
                .testTag("studio_home_screen")
        ) {
            when (val current = state) {
                is UiState.Loading -> SkeletonList(count = 4)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load Studio",
                    message = current.message,
                    onRetry = { viewModel.refreshHome(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "No projects yet",
                    subtitle = "Describe an app to start building or upload a ZIP workspace.",
                    actionLabel = "New project",
                    onActionClick = { showCreateSheet = true }
                )
                is UiState.Success -> {
                    val data = current.data
                    LazyColumn(
                        contentPadding = PaddingValues(Space.md),
                        verticalArrangement = Arrangement.spacedBy(Space.sm),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // AI Studio prompt card
                        item(key = "studio_prompt_hero") {
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
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(Space.xs))
                                        Text(
                                            text = "Start building in Studio",
                                            style = BansifyTextStyles.BodyStrong,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(Modifier.height(Space.xs))
                                    Text(
                                        text = "Chat to build apps with live Preview, Code explorer, and ${MockIntegrations.catalog.size}+ developer connections.",
                                        style = BansifyTextStyles.Caption,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(Space.sm))
                                    TextFieldOutlined(
                                        value = quickPrompt,
                                        onValueChange = { quickPrompt = it },
                                        placeholder = "Describe an app or feature to build…",
                                        singleLine = false,
                                        maxLines = 3,
                                        testTag = "studio_quick_prompt"
                                    )
                                    Spacer(Modifier.height(Space.sm))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        PrimaryButton(
                                            text = "Start project",
                                            onClick = {
                                                val created = viewModel.createProject(
                                                    name = quickPrompt.trim().take(28).ifEmpty { "New Studio App" },
                                                    prompt = quickPrompt.takeIf { it.isNotBlank() }
                                                )
                                                quickPrompt = ""
                                                onOpenRepo(created.id)
                                            },
                                            modifier = Modifier.weight(1f),
                                            testTag = "studio_start_project_btn"
                                        )
                                        TonalButton(
                                            text = "ZIP",
                                            leadingIcon = Icons.Rounded.FolderZip,
                                            onClick = { showZipSheet = true },
                                            testTag = "studio_zip_upload_button"
                                        )
                                    }
                                }
                            }
                        }

                        // Projects Section Header + Search/Sync
                        item(key = "projects_header") {
                            SectionHeader(
                                title = "Projects (${data.repos.size})",
                                actionLabel = if (data.syncedRecently) "Synced" else "Sync",
                                onActionClick = viewModel::syncNow
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextFieldOutlined(
                                    value = data.query,
                                    onValueChange = viewModel::onQueryChange,
                                    placeholder = "Search projects…",
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "studio_search_input"
                                )
                                IconPill(
                                    label = if (data.isSyncing) "Syncing…" else if (data.syncedRecently) "Synced" else "Sync",
                                    leadingIcon = if (data.syncedRecently) Icons.Rounded.Check else Icons.Rounded.Sync,
                                    onClick = viewModel::syncNow,
                                    testTag = "studio_sync_button"
                                )
                            }
                        }

                        if (data.isSyncing) {
                            item(key = "sync_skeleton") { SkeletonList(count = 2) }
                        } else if (data.repos.isEmpty()) {
                            item(key = "empty_filter") {
                                EmptyState(
                                    title = "No matching projects",
                                    subtitle = "Clear your search or start a new Studio project.",
                                    actionLabel = "Clear search",
                                    onActionClick = { viewModel.onQueryChange("") }
                                )
                            }
                        } else {
                            items(data.repos, key = { it.id }) { repo ->
                                ProjectCard(
                                    repo = repo,
                                    onClick = { onOpenRepo(repo.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        SheetScaffold(onDismiss = { showCreateSheet = false }, title = "New Studio Project") {
            TextFieldOutlined(
                value = newProjectName,
                onValueChange = { newProjectName = it.take(40) },
                placeholder = "e.g., Portfolio Dashboard",
                testTag = "studio_create_name_input"
            )
            Spacer(Modifier.height(Space.md))
            PrimaryButton(
                text = "Create & open workspace",
                onClick = {
                    val created = viewModel.createProject(newProjectName)
                    newProjectName = ""
                    showCreateSheet = false
                    onOpenRepo(created.id)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showZipSheet) {
        ZipUploadSheet(onDismiss = { showZipSheet = false })
    }
}

@Composable
private fun ProjectCard(
    repo: MockRepo,
    onClick: () -> Unit
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(BansifyRadius.Card)
            .clickable(onClick = onClick)
            .testTag("repo_card_${repo.id}")
    ) {
        Column(modifier = Modifier.padding(Space.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = repo.projectName,
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = repo.lastUpdated,
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Space.xxs))
            Text(
                text = repo.description,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Space.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(BansifyRadius.Full)
                            .background(Color(repo.languageColorHex))
                    )
                    Spacer(Modifier.width(Space.xxs))
                    Text(
                        text = repo.framework,
                        style = BansifyTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AccountTree,
                        contentDescription = "Repository",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(Space.xxs))
                    Text(
                        text = repo.fullName,
                        style = BansifyTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Hub,
                        contentDescription = "Connected integrations",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(Space.xxs))
                    Text(
                        text = "${repo.connectedIntegrationIds.size}",
                        style = BansifyTextStyles.CaptionStrong,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
