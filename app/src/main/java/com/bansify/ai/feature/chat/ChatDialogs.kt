package com.bansify.ai.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyAccent
import com.bansify.ai.core.designsystem.BansifyBrandEnd
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifySuccess
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.GeneratedMedia
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.MockRepo
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectPickerSheet(
    projects: List<MockRepo>,
    onSelectProject: (String) -> Unit,
    onOpenStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    SheetScaffold(onDismiss = onDismiss, title = "Change project") {
        projects.forEach { project ->
            SheetActionRow(
                icon = Icons.Outlined.Inventory2,
                title = project.projectName,
                subtitle = "${project.fullName} · ${project.activeBranch}"
            ) { onSelectProject(project.id) }
        }
        SheetActionRow(
            icon = Icons.Rounded.Code,
            title = "Browse all Studio projects",
            subtitle = "Create a new project or inspect deployments"
        ) { onOpenStudio() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessageOptionsSheet(
    message: ChatMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSelectText: () -> Unit,
    onRegenerate: () -> Unit,
    onReport: () -> Unit,
    onDismiss: () -> Unit
) {
    SheetScaffold(onDismiss = onDismiss, title = "Message options") {
        SheetActionRow(Icons.Rounded.ContentCopy, "Copy", null, onCopy)
        SheetActionRow(Icons.Rounded.Share, "Share (Demo)", null, onShare)
        SheetActionRow(Icons.Rounded.SelectAll, "Select text", null, onSelectText)
        if (message.role == MessageRole.ASSISTANT) {
            SheetActionRow(Icons.Rounded.Refresh, "Regenerate", null, onRegenerate)
        }
        SheetActionRow(Icons.Rounded.Flag, "Report (Demo)", null, onReport)
    }
}

@Composable
internal fun SelectMessageTextDialog(
    message: ChatMessage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BansifyRadius.SheetFull,
        title = { Text("Select text", style = BansifyTextStyles.H2) },
        text = { SelectionContainer { Text(text = message.content, style = BansifyTextStyles.Body) } },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", style = BansifyTextStyles.BodyStrong)
            }
        }
    )
}

@Composable
internal fun RenameChatDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BansifyRadius.SheetFull,
        title = { Text("Rename chat", style = BansifyTextStyles.H2) },
        text = {
            TextFieldOutlined(
                value = value,
                onValueChange = { onValueChange(it.take(48)) },
                placeholder = "Chat title",
                testTag = "rename_chat_input"
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save", style = BansifyTextStyles.BodyStrong) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", style = BansifyTextStyles.BodyStrong) }
        }
    )
}

@Composable
internal fun MediaPreviewDialog(
    media: GeneratedMedia,
    onCopyPrompt: () -> Unit,
    onSaveDemo: () -> Unit,
    onDismiss: () -> Unit
) {
    var isPlaying by remember(media) { mutableStateOf(media.isVideo) }
    var progress by remember(media) { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (media.isVideo && isPlaying) {
            while (isPlaying) {
                delay(120L)
                progress = if (progress >= 1f) 0f else (progress + 0.03f).coerceAtMost(1f)
            }
        }
    }

    val palettes = listOf(
        listOf(BansifyAccent, BansifyBrandEnd),
        listOf(BansifyBrandEnd, BansifySuccess),
        listOf(BansifyAccent, BansifySuccess)
    )
    val gradient = palettes[media.seedIndex.mod(palettes.size)]

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BansifyRadius.SheetFull,
        title = {
            Text(
                text = if (media.isVideo) "Video preview" else "Image preview",
                style = BansifyTextStyles.H2
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (media.isVideo) 16f / 9f else 1f)
                        .clip(BansifyRadius.Card)
                        .background(Brush.linearGradient(gradient))
                        .clickable(enabled = media.isVideo) { isPlaying = !isPlaying }
                ) {
                    if (media.isVideo) {
                        Surface(
                            shape = BansifyRadius.Full,
                            color = Color.Black.copy(alpha = 0.52f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(Space.sm)
                                .clip(BansifyRadius.Full)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Generated image preview",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                Text(
                    text = media.prompt,
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (media.isVideo) {
                        "Aspect ${media.aspectRatio} · ${media.durationLabel ?: "0:08 · 1080p"} · Demo render"
                    } else {
                        "Aspect ${media.aspectRatio} · High resolution · Demo render"
                    },
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                TextButton(onClick = onCopyPrompt) {
                    Text("Copy prompt", style = BansifyTextStyles.BodyStrong)
                }
                TextButton(onClick = onSaveDemo) {
                    Text("Save", style = BansifyTextStyles.BodyStrong)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
internal fun ChatOverflowMenu(
    expanded: Boolean,
    offlineDemoMode: Boolean,
    onDismiss: () -> Unit,
    onReplayDemo: () -> Unit,
    onClearChat: () -> Unit,
    onExportMarkdown: () -> Unit,
    onShareLink: () -> Unit,
    onToggleOffline: () -> Unit,
    onSimulateError: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        DropdownMenuItem(
            text = { Text("Replay streaming (Demo)", style = BansifyTextStyles.Body) },
            onClick = { onDismiss(); onReplayDemo() }
        )
        DropdownMenuItem(
            text = { Text("Clear chat", style = BansifyTextStyles.Body) },
            onClick = { onDismiss(); onClearChat() }
        )
        DropdownMenuItem(
            text = { Text("Export Markdown (Demo)", style = BansifyTextStyles.Body) },
            onClick = { onDismiss(); onExportMarkdown() }
        )
        DropdownMenuItem(
            text = { Text("Share link (Soon)", style = BansifyTextStyles.Body) },
            onClick = { onDismiss(); onShareLink() }
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (offlineDemoMode) "Go Online (Demo)" else "Offline banner (Demo)",
                    style = BansifyTextStyles.Body
                )
            },
            onClick = { onDismiss(); onToggleOffline() }
        )
        DropdownMenuItem(
            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
            onClick = { onDismiss(); onSimulateError() }
        )
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(BansifyRadius.Input)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.sm, vertical = Space.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Space.sm))
        Column {
            Text(text = title, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(text = subtitle, style = BansifyTextStyles.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
