package com.bansify.ai.feature.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Commit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.IconPill
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.data.mock.MockRepo
import com.bansify.ai.data.mock.RepoFileNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudioCodePane(
    repo: MockRepo,
    onOpenBranchPicker: () -> Unit,
    onSelectBranch: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }
    val subTabs = listOf("Files", "Branches", "Commits")

    Column(modifier = modifier.fillMaxSize().testTag("studio_code_pane")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = Space.xs)
        ) {
            IconPill(
                label = "${repo.fullName} · ${repo.activeBranch}",
                leadingIcon = Icons.Rounded.AccountTree,
                trailingIcon = Icons.Rounded.KeyboardArrowDown,
                onClick = onOpenBranchPicker,
                testTag = "repo_branch_chip"
            )
            Text(
                text = "${repo.files.size} files",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            subTabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedSubTab == idx,
                    onClick = { selectedSubTab = idx },
                    text = { Text(label, style = BansifyTextStyles.CaptionStrong, maxLines = 1) },
                    modifier = Modifier.testTag("repo_tab_${label.lowercase()}")
                )
            }
        }

        when (selectedSubTab) {
            0 -> CodeFilesList(repo.files, expandedFolders, onOpenFile)
            1 -> CodeBranchesList(repo.branches, repo.activeBranch, onSelectBranch)
            else -> CodeCommitsList(repo)
        }
    }
}

@Composable
private fun CodeFilesList(
    files: List<RepoFileNode>,
    expandedFolders: MutableMap<String, Boolean>,
    onOpenFile: (String) -> Unit
) {
    val rootFiles = remember(files) { files.filter { it.folder.isEmpty() } }
    val folderGroups = remember(files) {
        files.filter { it.folder.isNotEmpty() }.groupBy { it.folder }
    }

    LazyColumn(
        contentPadding = PaddingValues(Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = Modifier.fillMaxSize()
    ) {
        folderGroups.forEach { (folder, children) ->
            val expanded = expandedFolders[folder] ?: true
            item(key = "folder_$folder") {
                Surface(
                    shape = BansifyRadius.Input,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BansifyRadius.Input)
                        .clickable { expandedFolders[folder] = !expanded }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = Space.sm, vertical = Space.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = "Folder $folder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            text = "$folder/",
                            style = BansifyTextStyles.BodyStrong,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${children.size}",
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(Space.xxs))
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (expanded) {
                items(children, key = { it.path }) { file ->
                    CodeFileRow(file = file, indented = true, onClick = { onOpenFile(file.path) })
                }
            }
        }

        items(rootFiles, key = { it.path }) { file ->
            CodeFileRow(file = file, indented = false, onClick = { onOpenFile(file.path) })
        }
    }
}

@Composable
private fun CodeFileRow(
    file: RepoFileNode,
    indented: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indented) Space.md else 0.dp)
            .heightIn(min = 48.dp)
            .clip(BansifyRadius.Input)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.sm, vertical = Space.xs)
            .testTag("file_row_${file.name}")
    ) {
        Icon(
            imageVector = Icons.Rounded.Code,
            contentDescription = file.name,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(Space.sm))
        Text(
            text = file.name,
            style = BansifyTextStyles.Body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = file.sizeLabel,
            style = BansifyTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CodeBranchesList(
    branches: List<String>,
    activeBranch: String,
    onSelectBranch: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = Modifier.fillMaxSize()
    ) {
        items(branches, key = { it }) { branch ->
            val selected = branch == activeBranch
            Surface(
                shape = BansifyRadius.Card,
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BansifyRadius.Card)
                    .clickable { onSelectBranch(branch) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(Space.md)
                ) {
                    Icon(Icons.Rounded.AccountTree, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = branch,
                        style = BansifyTextStyles.BodyStrong,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected) {
                        Text("Active", style = BansifyTextStyles.CaptionStrong, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeCommitsList(repo: MockRepo) {
    LazyColumn(
        contentPadding = PaddingValues(Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = Modifier.fillMaxSize()
    ) {
        items(repo.commits, key = { it.hash }) { commit ->
            Surface(
                shape = BansifyRadius.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(Space.md)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Commit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(Space.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = commit.message, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(Space.xxs))
                        Text(
                            text = "${commit.hash} · ${commit.author} · ${commit.relativeTime}",
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
