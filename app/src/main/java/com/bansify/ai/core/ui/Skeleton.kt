package com.bansify.ai.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.Space

@Composable
fun rememberShimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate - 300f, 0f),
        end = Offset(translate, 200f)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: Shape = BansifyRadius.Chip
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun SkeletonList(
    count: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Space.sm),
        modifier = modifier
            .fillMaxWidth()
            .padding(Space.md)
            .testTag("skeleton_list")
    ) {
        repeat(count) { index ->
            Column(
                verticalArrangement = Arrangement.spacedBy(Space.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BansifyRadius.Card)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(Space.md)
            ) {
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(if (index % 2 == 0) 0.55f else 0.72f),
                    height = 18.dp
                )
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    height = 14.dp
                )
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.35f),
                    height = 12.dp
                )
            }
        }
    }
}

@Composable
fun SkeletonChatMessages(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        modifier = modifier
            .fillMaxWidth()
            .padding(Space.md)
            .testTag("skeleton_chat")
    ) {
        // Row 1: User pill
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            SkeletonBox(
                modifier = Modifier.fillMaxWidth(0.62f),
                height = 44.dp,
                shape = BansifyRadius.UserBubble
            )
        }
        // Row 2: Assistant block
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    height = 24.dp,
                    shape = BansifyRadius.Full
                )
                Spacer(Modifier.width(Space.xs))
                SkeletonBox(modifier = Modifier.width(96.dp), height = 14.dp)
            }
            SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 16.dp)
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.92f), height = 16.dp)
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.70f), height = 16.dp)
        }
        // Row 3: Second Assistant block
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    height = 24.dp,
                    shape = BansifyRadius.Full
                )
                Spacer(Modifier.width(Space.xs))
                SkeletonBox(modifier = Modifier.width(84.dp), height = 14.dp)
            }
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.96f), height = 16.dp)
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.54f), height = 16.dp)
        }
    }
}
