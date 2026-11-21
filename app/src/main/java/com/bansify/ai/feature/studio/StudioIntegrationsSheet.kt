package com.bansify.ai.feature.studio

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.StatusDot
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.data.mock.DeveloperIntegration
import com.bansify.ai.data.mock.IntegrationCategory
import com.bansify.ai.data.mock.MockIntegrations
import com.bansify.ai.data.mock.MockRepo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioIntegrationsSheet(
    projectName: String,
    connectedIds: Set<String>,
    onToggleIntegration: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<IntegrationCategory?>(null) }

    val filtered = remember(query, selectedCategory) {
        val q = query.trim().lowercase()
        MockIntegrations.catalog.filter { item ->
            val catMatch = selectedCategory == null || item.category == selectedCategory
            val qMatch = q.isEmpty() ||
                item.name.lowercase().contains(q) ||
                item.tagline.lowercase().contains(q) ||
                item.category.label.lowercase().contains(q)
            catMatch && qMatch
        }
    }

    SheetScaffold(
        onDismiss = onDismiss,
        title = "Project Connections (${connectedIds.size}/${MockIntegrations.catalog.size})"
    ) {
        Text(
            text = "Connect developer tools directly to $projectName chat, preview, and deployments.",
            style = BansifyTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Space.sm))

        TextFieldOutlined(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search GitHub, Cloudflare, Vercel, Figma…",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            testTag = "studio_integrations_search"
        )
        Spacer(Modifier.height(Space.xs))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${MockIntegrations.catalog.size})", style = BansifyTextStyles.CaptionStrong) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            IntegrationCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text(cat.label, style = BansifyTextStyles.CaptionStrong) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        Spacer(Modifier.height(Space.xs))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Space.xs),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                IntegrationRowCard(
                    item = item,
                    connected = item.id in connectedIds,
                    onToggle = { onToggleIntegration(item.id) }
                )
            }
        }
    }
}

@Composable
private fun IntegrationRowCard(
    item: DeveloperIntegration,
    connected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = BansifyRadius.Card,
        color = if (connected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (connected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline
        ),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("integration_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.sm)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(BansifyRadius.Input)
                    .background(
                        if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Text(
                    text = item.badgeShort,
                    style = BansifyTextStyles.CaptionStrong,
                    color = if (connected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(Space.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Text(
                        text = item.name,
                        style = BansifyTextStyles.BodyStrong,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (connected) {
                        StatusDot(status = HealthStatus.UP, size = 6.dp)
                    }
                }
                Text(
                    text = if (connected) item.connectionDetail else item.tagline,
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Space.xs))
            Switch(
                checked = connected,
                onCheckedChange = { onToggle() },
                thumbContent = if (connected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Connected",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
internal fun ProjectConnectionsBar(
    repo: MockRepo,
    onOpenBranch: () -> Unit,
    onOpenConnections: () -> Unit
) {
    val connectedTools = remember(repo.connectedIntegrationIds) {
        MockIntegrations.catalog.filter { it.id in repo.connectedIntegrationIds }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Space.md, vertical = Space.xs)
    ) {
        Surface(
            shape = BansifyRadius.Full,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            modifier = Modifier
                .clip(BansifyRadius.Full)
                .clickable(onClick = onOpenBranch)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs)
            ) {
                StatusDot(status = HealthStatus.UP, size = 6.dp)
                Spacer(Modifier.width(Space.xs))
                Text(
                    text = "${repo.fullName} · ${repo.activeBranch}",
                    style = BansifyTextStyles.CaptionStrong,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        connectedTools.filterNot { it.id == "github" }.forEach { tool ->
            Surface(
                shape = BansifyRadius.Full,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .clip(BansifyRadius.Full)
                    .clickable(onClick = onOpenConnections)
            ) {
                Text(
                    text = tool.name,
                    style = BansifyTextStyles.CaptionStrong,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs)
                )
            }
        }

        Surface(
            shape = BansifyRadius.Full,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 0.dp,
            modifier = Modifier
                .clip(BansifyRadius.Full)
                .clickable(onClick = onOpenConnections)
                .testTag("studio_connect_more_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Connect services",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(Space.xxs))
                Text(
                    text = "Connect (${MockIntegrations.catalog.size}+)",
                    style = BansifyTextStyles.CaptionStrong,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
