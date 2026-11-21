package com.bansify.ai.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.Space

/**
 * Expandable half-to-full-screen "Add to chat" sheet inspired by Claude's composer '+' menu.
 * Shared across standard Chat, Image/Video modes, and Studio Project Chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToChatSheet(
    activeProjectName: String,
    webSearchEnabled: Boolean,
    memoryEnabled: Boolean,
    onPickCamera: () -> Unit,
    onPickPhotos: () -> Unit,
    onPickFiles: () -> Unit,
    onRecordVideo: () -> Unit,
    onChangeProject: () -> Unit,
    onToggleWebSearch: (Boolean) -> Unit,
    onOpenConnectors: () -> Unit,
    onToggleMemory: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    // skipPartiallyExpanded = false allows half-screen open + drag-up to full-screen!
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BansifyRadius.Sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        scrimColor = BansifyThemeTokens.extendedColors.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = Space.md)
                .padding(bottom = Space.lg)
                .testTag("add_to_chat_sheet")
        ) {
            // Header: Close (X) left, "Add to chat" centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Space.md)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                        .testTag("add_sheet_close")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close add to chat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Add to chat",
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Top row: 3 equal square-ish media cards (Camera · Photos · Files)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                AddMediaSquareCard(
                    icon = Icons.Outlined.PhotoCamera,
                    label = "Camera",
                    tag = "add_sheet_camera",
                    onClick = onPickCamera,
                    modifier = Modifier.weight(1f)
                )
                AddMediaSquareCard(
                    icon = Icons.Outlined.Image,
                    label = "Photos",
                    tag = "add_sheet_photos",
                    onClick = onPickPhotos,
                    modifier = Modifier.weight(1f)
                )
                AddMediaSquareCard(
                    icon = Icons.Outlined.UploadFile,
                    label = "Files",
                    tag = "add_sheet_files",
                    onClick = onPickFiles,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Space.sm))

            // Full-width action cards
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                AddActionCard(
                    icon = Icons.Outlined.Videocam,
                    title = "Record video",
                    subtitle = null,
                    tag = "add_sheet_record_video",
                    onClick = onRecordVideo
                )

                AddActionCard(
                    icon = Icons.Outlined.Inventory2,
                    title = "Change project",
                    subtitle = activeProjectName,
                    subtitleAccent = true,
                    showChevron = true,
                    tag = "add_sheet_change_project",
                    onClick = onChangeProject
                )

                Spacer(Modifier.height(Space.xs))

                AddToggleCard(
                    icon = Icons.Outlined.Language,
                    title = "Web search",
                    checked = webSearchEnabled,
                    tag = "add_sheet_web_search",
                    onCheckedChange = onToggleWebSearch
                )

                AddActionCard(
                    icon = Icons.Outlined.Extension,
                    title = "Connectors",
                    subtitle = null,
                    showChevron = true,
                    tag = "add_sheet_connectors",
                    onClick = onOpenConnectors
                )

                AddToggleCard(
                    icon = Icons.Outlined.Psychology,
                    title = "Memory",
                    checked = memoryEnabled,
                    tag = "add_sheet_memory",
                    onCheckedChange = onToggleMemory
                )
            }
        }
    }
}

@Composable
private fun AddMediaSquareCard(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = modifier
            .heightIn(min = 108.dp)
            .clip(BansifyRadius.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = Space.md, horizontal = Space.xs)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(BansifyRadius.Full)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(Space.sm))
            Text(
                text = label,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    subtitleAccent: Boolean = false,
    showChevron: Boolean = false,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(BansifyRadius.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(BansifyRadius.Full)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Space.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = BansifyTextStyles.CaptionStrong,
                        color = if (subtitleAccent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AddToggleCard(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(BansifyRadius.Card)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(BansifyRadius.Full)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Space.sm))
            Text(
                text = title,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
