package com.bansify.ai.feature.models

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.bansify.ai.core.designsystem.HealthStatus
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.StatusDot
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.data.mock.ModelItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelRow(
    model: ModelItem,
    isSelected: Boolean,
    isDefault: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speedDots = "●".repeat(model.speedRating) + "○".repeat(3 - model.speedRating)
    val statusText = when (model.status) {
        HealthStatus.UP -> "Up"
        HealthStatus.SLOW -> "Slow"
        HealthStatus.DOWN -> "Down"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("model_row_${model.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = Space.md, vertical = Space.xs)
        ) {
            StatusDot(status = model.status)
            Spacer(Modifier.width(Space.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${model.contextLength} · $speedDots · $statusText",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FreeBadge(isFree = model.isFree)
            IconButton(
                onClick = onSelect,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("select_model_${model.id}")
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "${model.name} selected" else "Select ${model.name}",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = Space.md, vertical = Space.sm)
            ) {
                Text(
                    text = model.description,
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    verticalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    model.bestFor.forEach { tag ->
                        Surface(
                            shape = BansifyRadius.Chip,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = tag,
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = model.rateLimitNote,
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Space.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    TonalButton(
                        text = if (isDefault) "Default model ✓" else "Set as default",
                        onClick = onSetDefault,
                        enabled = !isDefault,
                        testTag = "set_default_${model.id}"
                    )
                }
            }
        }
    }
}
