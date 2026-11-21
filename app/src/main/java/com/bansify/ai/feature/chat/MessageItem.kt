package com.bansify.ai.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyAccent
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyBrandEnd
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifySuccess
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.GeneratedMedia
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MessageRole

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessageItem(
    message: ChatMessage,
    showActions: Boolean,
    onTapAssistant: () -> Unit,
    onLongPress: () -> Unit,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onFeedback: (MessageFeedback) -> Unit,
    onShare: () -> Unit,
    onCodeCopied: () -> Unit,
    onOpenMediaPreview: (GeneratedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.role == MessageRole.USER) {
        UserMessageBubble(
            message = message,
            onLongPress = onLongPress,
            modifier = modifier
        )
    } else {
        AssistantMessageBlock(
            message = message,
            showActions = showActions,
            onTap = onTapAssistant,
            onLongPress = onLongPress,
            onCopy = onCopy,
            onRegenerate = onRegenerate,
            onFeedback = onFeedback,
            onShare = onShare,
            onCodeCopied = onCodeCopied,
            onOpenMediaPreview = onOpenMediaPreview,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun UserMessageBubble(
    message: ChatMessage,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_message_${message.id}")
    ) {
        Surface(
            shape = BansifyRadius.UserBubble,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp,
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(BansifyRadius.UserBubble)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm)
            ) {
                if (message.attachments.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                        verticalArrangement = Arrangement.spacedBy(Space.xxs)
                    ) {
                        message.attachments.forEach { att ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(BansifyRadius.Chip)
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                                    .padding(horizontal = Space.xs, vertical = Space.xxs)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(Space.xxs))
                                Text(
                                    text = att.label,
                                    style = BansifyTextStyles.Caption,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Space.xs))
                }
                Text(
                    text = message.content,
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistantMessageBlock(
    message: ChatMessage,
    showActions: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onFeedback: (MessageFeedback) -> Unit,
    onShare: () -> Unit,
    onCodeCopied: () -> Unit,
    onOpenMediaPreview: (GeneratedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BansifyRadius.Card)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(vertical = Space.xxs)
            .testTag("assistant_message_${message.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            BansifyBrandAvatar(size = 24.dp)
            Text(
                text = message.modelName ?: "Bansify AI",
                style = BansifyTextStyles.CaptionStrong,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "· ${message.timestampLabel}",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(Space.xs))

        val media = message.generatedMedia
        if (media != null) {
            GeneratedMediaCard(
                media = media,
                isGenerating = message.isStreaming,
                onClick = { if (!message.isStreaming) onOpenMediaPreview(media) }
            )
            Spacer(Modifier.height(Space.xs))
        }

        MarkdownLiteText(
            markdown = message.content,
            isStreaming = message.isStreaming && media == null,
            onCopyCode = { onCodeCopied() }
        )

        AnimatedVisibility(
            visible = showActions && !message.isStreaming,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xxs),
                modifier = Modifier.padding(top = Space.xs)
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy response",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onRegenerate, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Regenerate response",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onFeedback(MessageFeedback.UP) },
                    modifier = Modifier.size(48.dp)
                ) {
                    val liked = message.feedback == MessageFeedback.UP
                    Icon(
                        imageVector = if (liked) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Helpful response",
                        tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onFeedback(MessageFeedback.DOWN) },
                    modifier = Modifier.size(48.dp)
                ) {
                    val disliked = message.feedback == MessageFeedback.DOWN
                    Icon(
                        imageVector = if (disliked) Icons.Rounded.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Needs improvement",
                        tint = if (disliked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share response",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneratedMediaCard(
    media: GeneratedMedia,
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    val palettes = listOf(
        listOf(BansifyAccent, BansifyBrandEnd),
        listOf(BansifyBrandEnd, BansifySuccess),
        listOf(BansifyAccent, BansifySuccess)
    )
    val gradient = palettes[media.seedIndex.mod(palettes.size)]
    val ratio = if (media.isVideo) 16f / 9f else 4f / 3f

    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(BansifyRadius.Card)
            .clickable(enabled = !isGenerating, onClick = onClick)
            .testTag(if (media.isVideo) "generated_video_card" else "generated_image_card")
    ) {
        if (isGenerating) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Space.lg)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(Space.sm))
                Text(
                    text = if (media.isVideo) "Rendering video clip…" else "Creating image…",
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.sm))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .clip(BansifyRadius.Full),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradient))
                    .padding(Space.md)
            ) {
                Surface(
                    shape = BansifyRadius.Chip,
                    color = Color.Black.copy(alpha = 0.42f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
                        modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
                    ) {
                        Icon(
                            imageVector = if (media.isVideo) Icons.Outlined.Videocam else Icons.Outlined.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (media.isVideo) {
                                "VIDEO · ${media.durationLabel ?: "0:08"}"
                            } else {
                                "IMAGE · ${media.aspectRatio}"
                            },
                            style = BansifyTextStyles.CaptionStrong,
                            color = Color.White
                        )
                    }
                }

                if (media.isVideo) {
                    Surface(
                        shape = BansifyRadius.Full,
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play generated video",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Generated image artwork",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }

                Text(
                    text = media.prompt,
                    style = BansifyTextStyles.Caption,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun DateSeparator(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.xs)
    ) {
        Surface(
            shape = BansifyRadius.Full,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp
        ) {
            Text(
                text = label,
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs)
            )
        }
    }
}
