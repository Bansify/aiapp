package com.bansify.ai.feature.comingsoon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space

/**
 * Every surface that is not chat yet.
 *
 * Chat is live on the Bansify AI Worker; Studio, Workspace, the model catalog and
 * the rest ship in the next release. Showing an honest "coming soon" is better
 * than a mock screen that looks finished and does nothing.
 */
@Composable
fun ComingSoonScreen(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.padding(Space.xs)) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Space.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.height(Space.md))
                Text(
                    text = "Coming soon",
                    style = BansifyTextStyles.CaptionStrong,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = title,
                    style = BansifyTextStyles.H2,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Space.sm))
                Text(
                    text = subtitle,
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(Space.lg))
                    PrimaryButton(
                        text = actionLabel,
                        onClick = onAction,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "coming_soon_action",
                    )
                }
            }
        }
    }
}
