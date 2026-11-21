package com.bansify.ai.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.FreeBadge
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.StatusDot
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.data.mock.ModelItem
import com.bansify.ai.data.mock.ProviderGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSheet(
    providers: List<ProviderGroup>,
    autoModel: ModelItem,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    onSeeAllModels: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var query by remember { mutableStateOf("") }
    val expandedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            providers.take(3).forEach { put(it.id, true) }
        }
    }

    val filteredProviders = remember(providers, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            providers
        } else {
            providers.mapNotNull { group ->
                val matchesProvider = group.name.lowercase().contains(q)
                val matchedModels = if (matchesProvider) {
                    group.models
                } else {
                    group.models.filter { it.name.lowercase().contains(q) }
                }
                if (matchedModels.isEmpty()) null else group.copy(models = matchedModels)
            }
        }
    }

    SheetScaffold(
        onDismiss = onDismiss,
        title = "Model",
        sheetState = sheetState
    ) {
        TextFieldOutlined(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search models or providers",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            testTag = "model_sheet_search"
        )
        Spacer(Modifier.height(Space.sm))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Space.xs),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
                .testTag("model_sheet_list")
        ) {
            if (query.isBlank() || "auto".contains(query.trim().lowercase())) {
                item(key = "auto_pinned") {
                    val selected = selectedModelId == autoModel.id
                    Surface(
                        shape = BansifyRadius.Card,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(BansifyRadius.Card)
                            .clickable {
                                onSelectModel(autoModel.id)
                                onDismiss()
                            }
                            .testTag("model_sheet_auto_row")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(Space.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(Space.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto (recommended)",
                                    style = BansifyTextStyles.BodyStrong,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fastest available",
                                    style = BansifyTextStyles.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FreeBadge(isFree = true)
                            if (selected) {
                                Spacer(Modifier.width(Space.xs))
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredProviders.isEmpty()) {
                item(key = "empty_models_sheet") {
                    EmptyState(
                        title = "No models found",
                        subtitle = "Try a different search term or view all models."
                    )
                }
            } else {
                items(filteredProviders, key = { it.id }) { provider ->
                    val expanded = query.isNotBlank() || (expandedMap[provider.id] == true)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(BansifyRadius.Input)
                                .clickable { expandedMap[provider.id] = !expanded }
                                .padding(horizontal = Space.xs, vertical = Space.xs)
                        ) {
                            StatusDot(status = provider.status)
                            Spacer(Modifier.width(Space.xs))
                            Text(
                                text = provider.name,
                                style = BansifyTextStyles.BodyStrong,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${provider.models.size}",
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(Space.xxs))
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (expanded) "Collapse ${provider.name}" else "Expand ${provider.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (expanded) {
                            provider.models.forEach { model ->
                                val isSelected = model.id == selectedModelId
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .clip(BansifyRadius.Input)
                                        .clickable {
                                            onSelectModel(model.id)
                                            onDismiss()
                                        }
                                        .padding(horizontal = Space.sm, vertical = Space.xs)
                                        .testTag("sheet_model_${model.id}")
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
                                            text = "${model.contextLength} · ${"●".repeat(model.speedRating)}${"○".repeat(3 - model.speedRating)}",
                                            style = BansifyTextStyles.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FreeBadge(isFree = model.isFree)
                                    if (isSelected) {
                                        Spacer(Modifier.width(Space.xs))
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Space.xs))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            BansifyTextButton(
                text = "View all models →",
                onClick = {
                    onDismiss()
                    onSeeAllModels()
                },
                testTag = "model_sheet_see_all"
            )
        }
    }
}
