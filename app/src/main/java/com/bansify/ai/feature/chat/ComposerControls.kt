package com.bansify.ai.feature.chat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space

@Composable
internal fun ComposerCircleButton(
    icon: ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(BansifyRadius.Full)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun ComposerPrimaryActionCircle(
    mode: String,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onWave: () -> Unit
) {
    val bgColor = when (mode) {
        "send" -> MaterialTheme.colorScheme.primary
        "stop" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val fgColor = when (mode) {
        "send" -> MaterialTheme.colorScheme.onPrimary
        "stop" -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.background
    }
    val icon = when (mode) {
        "send" -> Icons.Rounded.ArrowUpward
        "stop" -> Icons.Rounded.Stop
        else -> Icons.Rounded.GraphicEq
    }
    val description = when (mode) {
        "send" -> "Send message"
        "stop" -> "Stop generating"
        else -> "Start voice conversation"
    }
    val tag = when (mode) {
        "send" -> "composer_send_button"
        "stop" -> "composer_stop_button"
        else -> "composer_wave_button"
    }

    IconButton(
        onClick = {
            when (mode) {
                "send" -> onSend()
                "stop" -> onStop()
                else -> onWave()
            }
        },
        modifier = Modifier
            .size(48.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(BansifyRadius.Full)
                .background(bgColor)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = fgColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun VoiceListeningBar(
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "voice_wave")
    val barScales = listOf(0, 120, 240, 360, 180).map { delayMs ->
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 480, delayMillis = delayMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$delayMs"
        )
    }

    Surface(
        shape = BansifyRadius.Composer,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_listening_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.xs, vertical = Space.xxs)
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("voice_cancel_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cancel voice input",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xxs),
                modifier = Modifier.padding(horizontal = Space.xs)
            ) {
                barScales.forEach { scale ->
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height((22f * scale.value).dp)
                            .clip(BansifyRadius.Full)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(Modifier.width(Space.xs))
            Text(
                text = "Listening… (Demo)",
                style = BansifyTextStyles.BodyStrong,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(BansifyRadius.Full)
                    .clickable(onClick = onDone)
                    .testTag("voice_done_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(BansifyRadius.Full)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Insert voice transcript",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
