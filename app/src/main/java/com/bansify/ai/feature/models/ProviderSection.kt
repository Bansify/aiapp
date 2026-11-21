package com.bansify.ai.feature.models

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.FreeBadge
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.StatusDot
import com.bansify.ai.data.mock.ProviderGroup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderSection(
    provider: ProviderGroup,
    isCollapsed: Boolean,
    selectedModelId: String,
    defaultModelId: String,
    expandedModelId: String?,
    onToggleCollapse: () -> Unit,
    onToggleModelExpand: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onSetDefaultModel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("provider_section_${provider.id}")
    ) {
        Column {
            ProviderHeader(
                provider = provider,
                isCollapsed = isCollapsed,
                onClick = onToggleCollapse
            )

            AnimatedVisibility(visible = !isCollapsed) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    provider.models.forEachIndexed { idx, model ->
                        ModelRow(
                            model = model,
                            isSelected = model.id == selectedModelId,
                            isDefault = model.id == defaultModelId,
                            isExpanded = expandedModelId == model.id,
                            onToggleExpand = { onToggleModelExpand(model.id) },
                            onSelect = { onSelectModel(model.id) },
                            onSetDefault = { onSetDefaultModel(model.id) }
                        )
                        if (idx < provider.models.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = Space.md)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.xs)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Space.xs),
                            verticalArrangement = Arrangement.spacedBy(Space.xxs),
                            modifier = Modifier.weight(1f)
                        ) {
                            provider.quotaChips.forEach { quota ->
                                Surface(
                                    shape = BansifyRadius.Chip,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 0.dp
                                ) {
                                    Text(
                                        text = quota,
                                        style = BansifyTextStyles.Caption,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
                                    )
                                }
                            }
                        }
                        if (provider.resetHint != null) {
                            Spacer(Modifier.width(Space.xs))
                            Text(
                                text = provider.resetHint,
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderHeader(
    provider: ProviderGroup,
    isCollapsed: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.sm)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(BansifyRadius.Chip)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = provider.monogram,
                style = BansifyTextStyles.CaptionStrong,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(Space.sm))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = provider.name,
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(Space.xs))
                StatusDot(status = provider.status)
            }
            Text(
                text = "${provider.models.size} models",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FreeBadge(
            isFree = provider.isCardFreeTier,
            customLabel = provider.freeTierSummary
        )
        Spacer(Modifier.width(Space.xs))
        Icon(
            imageVector = if (isCollapsed) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
            contentDescription = if (isCollapsed) "Expand ${provider.name}" else "Collapse ${provider.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
