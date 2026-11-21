package com.bansify.ai.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TonalButton

@Composable
fun ErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Try again"
) {
    val extended = BansifyThemeTokens.extendedColors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(Space.xl)
            .testTag("error_state")
    ) {
        Surface(
            color = extended.dangerContainer,
            shape = BansifyRadius.Card,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Space.lg)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = "Error",
                    tint = extended.danger,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(Space.sm))
                Text(
                    text = title,
                    style = BansifyTextStyles.H2,
                    color = extended.onDangerContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Space.xxs))
                Text(
                    text = message,
                    style = BansifyTextStyles.Body,
                    color = extended.onDangerContainer.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Space.md))
                PrimaryButton(
                    text = retryLabel,
                    onClick = onRetry,
                    leadingIcon = Icons.Rounded.Refresh,
                    testTag = "error_retry_button"
                )
            }
        }
    }
}

@Composable
fun InlineErrorCard(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = BansifyThemeTokens.extendedColors
    Surface(
        shape = BansifyRadius.Card,
        color = extended.dangerContainer,
        border = BorderStroke(1.dp, extended.danger.copy(alpha = 0.35f)),
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("inline_error_card")
    ) {
        Column(modifier = Modifier.padding(Space.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = "Error",
                    tint = extended.danger,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Space.xs))
                Text(
                    text = title,
                    style = BansifyTextStyles.BodyStrong,
                    color = extended.onDangerContainer
                )
            }
            Spacer(Modifier.height(Space.xxs))
            Text(
                text = message,
                style = BansifyTextStyles.Caption,
                color = extended.onDangerContainer.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(Space.sm))
            TonalButton(
                text = "Retry",
                onClick = onRetry,
                leadingIcon = Icons.Rounded.Refresh,
                testTag = "inline_error_retry"
            )
        }
    }
}
