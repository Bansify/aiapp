package com.bansify.ai.feature.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Stop
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.IconPill
import com.bansify.ai.core.designsystem.Motion
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.data.mock.ChatAttachment
import com.bansify.ai.data.mock.ChatMode

@Composable
fun ChatComposer(
    text: String,
    attachments: List<ChatAttachment>,
    chatMode: ChatMode,
    selectedModelLabel: String,
    isStreaming: Boolean,
    isListening: Boolean,
    sendOnEnter: Boolean,
    onTextChange: (String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onResetChatMode: () -> Unit,
    onModelClick: () -> Unit,
    onAttachClick: () -> Unit,
    onMicClick: () -> Unit,
    onCancelListening: () -> Unit,
    onFinishListening: () -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var pendingFocusRequest by remember { mutableStateOf(false) }

    val isExpanded = isFocused || text.isNotEmpty() || attachments.isNotEmpty() || isStreaming

    LaunchedEffect(isExpanded, pendingFocusRequest) {
        if (isExpanded && pendingFocusRequest) {
            pendingFocusRequest = false
            focusRequester.requestFocus()
        }
    }

    val idlePlaceholder = when (chatMode) {
        ChatMode.CHAT -> "Chat with Bansify"
        ChatMode.IMAGE -> "Describe an image for Bansify"
        ChatMode.VIDEO -> "Describe a video for Bansify"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = Space.md, vertical = Space.xs)
    ) {
        AnimatedVisibility(
            visible = chatMode != ChatMode.CHAT,
            enter = fadeIn(tween(Motion.Micro)),
            exit = fadeOut(tween(Motion.Micro))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Space.xs)
            ) {
                Surface(
                    shape = BansifyRadius.Full,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("composer_mode_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = Space.sm, end = Space.xxs)
                    ) {
                        Icon(
                            imageVector = if (chatMode == ChatMode.VIDEO) Icons.Outlined.Videocam else Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(Space.xs))
                        Text(
                            text = if (chatMode == ChatMode.VIDEO) "Video mode" else "Image mode",
                            style = BansifyTextStyles.CaptionStrong,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = onResetChatMode, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Switch back to chat mode",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isStreaming,
            enter = fadeIn(tween(Motion.Micro)),
            exit = fadeOut(tween(Motion.Micro))
        ) {
            Box(modifier = Modifier.padding(bottom = Space.xs)) {
                IconPill(
                    label = "Stop",
                    leadingIcon = Icons.Rounded.Stop,
                    onClick = onStopClick,
                    testTag = "stop_streaming_pill"
                )
            }
        }

        AnimatedVisibility(visible = attachments.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = Space.xs)
            ) {
                attachments.forEach { att ->
                    Surface(
                        shape = BansifyRadius.Chip,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = Space.xs, end = Space.xxs, top = Space.xxs, bottom = Space.xxs)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AttachFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(Space.xxs))
                            Text(
                                text = att.label,
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onRemoveAttachment(att.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove attachment",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isListening) {
            VoiceListeningBar(onCancel = onCancelListening, onDone = onFinishListening)
            return
        }

        val hasContent = text.isNotBlank() || attachments.isNotEmpty()
        val cardShape = if (isExpanded) RoundedCornerShape(26.dp) else RoundedCornerShape(32.dp)

        // Two-state Claude-style morphing composer surface
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(Motion.Screen))
                .testTag("chat_composer_pill")
        ) {
            if (!isExpanded) {
                // State 1: Compact idle single-row pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.xs, vertical = Space.xxs)
                ) {
                    ComposerCircleButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = "Add to chat",
                        tag = "composer_attach_button",
                        onClick = onAttachClick
                    )
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable {
                                pendingFocusRequest = true
                                isFocused = true
                            }
                            .padding(horizontal = Space.xs)
                            .testTag("composer_idle_tap_target")
                    ) {
                        Text(
                            text = idlePlaceholder,
                            style = BansifyTextStyles.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ComposerCircleButton(
                        icon = Icons.Outlined.Mic,
                        contentDescription = "Start voice input",
                        tag = "composer_mic_button",
                        onClick = onMicClick
                    )
                    ComposerPrimaryActionCircle(
                        mode = "wave",
                        onSend = onSendClick,
                        onStop = onStopClick,
                        onWave = onMicClick
                    )
                }
            } else {
                // State 2: Expanded two-row typing card with inline model selector pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.sm, vertical = Space.sm)
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .padding(horizontal = Space.xs)
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = idlePlaceholder,
                                style = BansifyTextStyles.Body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            textStyle = BansifyTextStyles.Body.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            maxLines = 6,
                            keyboardOptions = KeyboardOptions(
                                imeAction = if (sendOnEnter) ImeAction.Send else ImeAction.Default
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { if (sendOnEnter && !isStreaming) onSendClick() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { state -> isFocused = state.isFocused }
                                .testTag("composer_text_field")
                        )
                    }

                    Spacer(Modifier.height(Space.xs))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ComposerCircleButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "Add to chat",
                            tag = "composer_attach_button",
                            onClick = onAttachClick
                        )
                        Spacer(Modifier.width(Space.xxs))
                        Surface(
                            shape = BansifyRadius.Full,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .clip(BansifyRadius.Full)
                                .clickable(role = Role.Button, onClick = onModelClick)
                                .testTag("chat_model_chip")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs)
                            ) {
                                Text(
                                    text = selectedModelLabel,
                                    style = BansifyTextStyles.CaptionStrong,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        ComposerCircleButton(
                            icon = Icons.Outlined.Mic,
                            contentDescription = "Start voice input",
                            tag = "composer_mic_button",
                            onClick = onMicClick
                        )

                        val actionMode = when {
                            isStreaming -> "stop"
                            hasContent -> "send"
                            else -> "wave"
                        }
                        AnimatedContent(
                            targetState = actionMode,
                            transitionSpec = {
                                (fadeIn(tween(Motion.Micro)) + scaleIn(tween(Motion.Micro), initialScale = 0.85f)) togetherWith
                                    (fadeOut(tween(Motion.Micro)) + scaleOut(tween(Motion.Micro), targetScale = 0.85f))
                            },
                            label = "composer_action_button"
                        ) { mode ->
                            ComposerPrimaryActionCircle(
                                mode = mode,
                                onSend = onSendClick,
                                onStop = onStopClick,
                                onWave = onMicClick
                            )
                        }
                    }
                }
            }
        }
    }
}
