package com.bansify.ai.feature.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Motion
import com.bansify.ai.core.designsystem.Space
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isOnboarded: Boolean?,
    onFinished: (goToOnboarding: Boolean) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = Motion.Screen, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )

    LaunchedEffect(isOnboarded) {
        visible = true
        if (isOnboarded != null) {
            delay(750L)
            onFinished(!isOnboarded)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(Space.xl)
            .testTag("splash_screen")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {
            BansifyBrandAvatar(size = 72.dp)
            Spacer(Modifier.height(Space.lg))
            Text(
                text = "Bansify AI",
                style = BansifyTextStyles.Splash,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                text = "Thoughtful, fast, yours.",
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
