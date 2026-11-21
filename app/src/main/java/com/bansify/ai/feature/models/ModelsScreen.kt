package com.bansify.ai.feature.models

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.state.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    viewModel: ModelsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val collapsedMap = remember { mutableStateMapOf<String, Boolean>() }
    var expandedModelId by remember { mutableStateOf<String?>(null) }
    var showAddProviderSheet by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BansifyTopBar(
                title = "Models",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { showFilterMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("models_filter_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Filter or refresh models",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh health (Loading demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showFilterMenu = false
                                viewModel.refresh(simulateError = false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showFilterMenu = false
                                viewModel.refresh(simulateError = true)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("models_screen")
        ) {
            when (val current = state) {
                is UiState.Loading -> SkeletonList(count = 5)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load models",
                    message = current.message,
                    onRetry = { viewModel.refresh(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "No models available",
                    subtitle = "Refresh to reload the provider catalog.",
                    actionLabel = "Refresh",
                    onActionClick = { viewModel.refresh(false) }
                )
                is UiState.Success -> {
                    val data = current.data
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.xs)
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ModelFilter.entries.forEachIndexed { index, filter ->
                                SegmentedButton(
                                    selected = data.filter == filter,
                                    onClick = { viewModel.onFilterChange(filter) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ModelFilter.entries.size
                                    ),
                                    modifier = Modifier.testTag("models_seg_${filter.name.lowercase()}")
                                ) {
                                    Text(
                                        text = filter.label,
                                        style = BansifyTextStyles.CaptionStrong,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(Space.sm))
                        TextFieldOutlined(
                            value = data.query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = "Search models or providers…",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            testTag = "models_search_field"
                        )
                    }

                    val cardFree = remember(data.providers) { data.providers.filter { it.isCardFreeTier } }
                    val paidProviders = remember(data.providers) { data.providers.filterNot { it.isCardFreeTier } }

                    if (data.providers.isEmpty()) {
                        EmptyState(
                            title = "No models found",
                            subtitle = "Clear your filters or search query to view all 30+ models.",
                            actionLabel = "Reset filters",
                            onActionClick = {
                                viewModel.onFilterChange(ModelFilter.ALL)
                                viewModel.onQueryChange("")
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Space.md),
                            verticalArrangement = Arrangement.spacedBy(Space.sm),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (cardFree.isNotEmpty()) {
                                item(key = "hdr_card_free") {
                                    SectionHeader(title = "Card-free tier (${cardFree.size} providers)")
                                }
                                items(cardFree, key = { it.id }) { provider ->
                                    ProviderSection(
                                        provider = provider,
                                        isCollapsed = collapsedMap[provider.id] == true,
                                        selectedModelId = data.selectedModelId,
                                        defaultModelId = data.defaultModelId,
                                        expandedModelId = expandedModelId,
                                        onToggleCollapse = {
                                            collapsedMap[provider.id] = !(collapsedMap[provider.id] == true)
                                        },
                                        onToggleModelExpand = { id ->
                                            expandedModelId = if (expandedModelId == id) null else id
                                        },
                                        onSelectModel = { id ->
                                            viewModel.selectModel(id)
                                            scope.launch { snackbarHostState.showSnackbar("Active chat model updated") }
                                        },
                                        onSetDefaultModel = { id ->
                                            viewModel.setDefaultModel(id)
                                            scope.launch { snackbarHostState.showSnackbar("Saved as default model") }
                                        }
                                    )
                                }
                            }

                            if (paidProviders.isNotEmpty()) {
                                item(key = "hdr_paid") {
                                    Spacer(Modifier.height(Space.xs))
                                    SectionHeader(title = "Paid / card required (${paidProviders.size} providers)")
                                }
                                items(paidProviders, key = { it.id }) { provider ->
                                    ProviderSection(
                                        provider = provider,
                                        isCollapsed = collapsedMap[provider.id] == true,
                                        selectedModelId = data.selectedModelId,
                                        defaultModelId = data.defaultModelId,
                                        expandedModelId = expandedModelId,
                                        onToggleCollapse = {
                                            collapsedMap[provider.id] = !(collapsedMap[provider.id] == true)
                                        },
                                        onToggleModelExpand = { id ->
                                            expandedModelId = if (expandedModelId == id) null else id
                                        },
                                        onSelectModel = { id ->
                                            viewModel.selectModel(id)
                                            scope.launch { snackbarHostState.showSnackbar("Selected (Setup needed · Demo)") }
                                        },
                                        onSetDefaultModel = { id ->
                                            viewModel.setDefaultModel(id)
                                            scope.launch { snackbarHostState.showSnackbar("Saved as default model") }
                                        }
                                    )
                                }
                            }

                            item(key = "add_custom_provider_card") {
                                Spacer(Modifier.height(Space.xs))
                                Surface(
                                    shape = BansifyRadius.Card,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(BansifyRadius.Card)
                                        .clickable { showAddProviderSheet = true }
                                        .testTag("add_provider_card")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(Space.md)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add provider",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(Space.sm))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Add provider",
                                                style = BansifyTextStyles.BodyStrong,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "OpenAI-compatible endpoint (Demo — local only)",
                                                style = BansifyTextStyles.Caption,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(Space.lg))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProviderSheet) {
        AddProviderSheet(
            onDismiss = { showAddProviderSheet = false },
            onSave = { name, url, models ->
                viewModel.addCustomProvider(name, url, models)
                showAddProviderSheet = false
                scope.launch { snackbarHostState.showSnackbar("Demo provider \"$name\" added locally") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProviderSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, baseUrl: String, modelsRaw: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var modelsList by remember { mutableStateOf("") }
    var showValidation by remember { mutableStateOf(false) }

    val valid = name.isNotBlank() && baseUrl.isNotBlank() && apiKey.isNotBlank() && modelsList.isNotBlank()

    SheetScaffold(
        onDismiss = onDismiss,
        title = "Add custom provider (Demo)"
    ) {
        Text(
            text = "Non-functional local demo form — validates inputs and appends to catalog without making network calls.",
            style = BansifyTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Space.sm))
        TextFieldOutlined(
            value = name,
            onValueChange = { name = it },
            label = "Provider name",
            placeholder = "e.g. Local Ollama",
            isError = showValidation && name.isBlank(),
            testTag = "custom_provider_name"
        )
        Spacer(Modifier.height(Space.xs))
        TextFieldOutlined(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = "Base URL",
            placeholder = "https://api.example.com/v1",
            isError = showValidation && baseUrl.isBlank(),
            testTag = "custom_provider_url"
        )
        Spacer(Modifier.height(Space.xs))
        TextFieldOutlined(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = "API Key (Demo — not sent anywhere)",
            placeholder = "demo-key-placeholder",
            isError = showValidation && apiKey.isBlank(),
            testTag = "custom_provider_key"
        )
        Spacer(Modifier.height(Space.xs))
        TextFieldOutlined(
            value = modelsList,
            onValueChange = { modelsList = it },
            label = "Model list (comma separated)",
            placeholder = "llama-3.2-local, qwen-coder",
            isError = showValidation && modelsList.isBlank(),
            testTag = "custom_provider_models"
        )
        Spacer(Modifier.height(Space.md))
        PrimaryButton(
            text = "Save demo provider",
            onClick = {
                if (valid) {
                    onSave(name, baseUrl, modelsList)
                } else {
                    showValidation = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "custom_provider_save"
        )
    }
}
