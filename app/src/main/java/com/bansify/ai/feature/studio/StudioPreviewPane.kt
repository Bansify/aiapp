package com.bansify.ai.feature.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.StatusDot
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.data.mock.MockIntegrations
import com.bansify.ai.data.mock.MockRepo
import kotlinx.coroutines.delay

@Composable
internal fun StudioPreviewPane(
    repo: MockRepo,
    onBackToChat: () -> Unit,
    onOpenConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMobileViewport by remember { mutableStateOf(true) }
    var isReloading by remember { mutableStateOf(false) }
    var previewCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(isReloading) {
        if (isReloading) {
            delay(550L)
            isReloading = false
        }
    }

    val connectedNames = remember(repo.connectedIntegrationIds) {
        MockIntegrations.catalog
            .filter { it.id in repo.connectedIntegrationIds }
            .map { it.name }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Space.md)
            .testTag("studio_preview_pane")
    ) {
        // Preview URL & viewport bar
        Surface(
            shape = BansifyRadius.Card,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs)
            ) {
                StatusDot(status = HealthStatus.UP)
                Spacer(Modifier.width(Space.xs))
                Text(
                    text = repo.previewUrl,
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { isMobileViewport = !isMobileViewport },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isMobileViewport) {
                            Icons.Outlined.Smartphone
                        } else {
                            Icons.Outlined.DesktopWindows
                        },
                        contentDescription = "Toggle mobile or desktop preview",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { isReloading = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reload preview",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(Space.md))

        // Interactive mock preview frame
        Surface(
            shape = BansifyRadius.SheetFull,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 0.dp,
            modifier = Modifier
                .then(if (isMobileViewport) Modifier.widthIn(max = 360.dp) else Modifier.fillMaxWidth())
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Space.md)) {
                if (isReloading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(BansifyRadius.Full),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(Space.md))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    BansifyBrandAvatar(size = 32.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = repo.projectName,
                            style = BansifyTextStyles.BodyStrong,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${repo.framework} · ${repo.activeBranch}",
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = BansifyRadius.Full,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "LIVE",
                            style = BansifyTextStyles.CaptionStrong,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
                        )
                    }
                }

                Spacer(Modifier.height(Space.md))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BansifyRadius.Card)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(Space.md)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Space.xs))
                            Text(
                                text = "Interactive Preview Canvas",
                                style = BansifyTextStyles.BodyStrong,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = repo.description,
                            style = BansifyTextStyles.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Space.xs))
                        PrimaryButton(
                            text = if (previewCounter == 0) "Test interactive action" else "Tapped $previewCounter times",
                            onClick = { previewCounter++ },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "preview_interactive_button"
                        )
                    }
                }

                Spacer(Modifier.height(Space.md))

                Text(
                    text = "Connected services (${connectedNames.size})",
                    style = BansifyTextStyles.CaptionStrong,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Space.xxs))
                Text(
                    text = connectedNames.joinToString(" · ").ifEmpty { "No services connected yet" },
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(Space.md))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TonalButton(
                        text = "Edit via Chat",
                        onClick = onBackToChat,
                        modifier = Modifier.weight(1f)
                    )
                    TonalButton(
                        text = "Connections",
                        onClick = onOpenConnections,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
