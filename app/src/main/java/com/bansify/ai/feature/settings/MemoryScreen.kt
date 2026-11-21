package com.bansify.ai.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.ConfirmDialog
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.data.mock.MemoryFact
import com.bansify.ai.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MemoryScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val facts by viewModel.memoryFacts.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var loadState by remember { mutableStateOf<UiState<List<MemoryFact>>>(UiState.Loading) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    fun load(simulateError: Boolean = false) {
        scope.launch {
            loadState = UiState.Loading
            delay(620L)
            loadState = when {
                simulateError -> UiState.Error("Memory index load fail (mock).")
                facts.isEmpty() -> UiState.Empty
                else -> UiState.Success(facts)
            }
        }
    }

    LaunchedEffect(Unit) { load(false) }
    LaunchedEffect(facts) {
        if (loadState is UiState.Success || loadState is UiState.Empty) {
            loadState = if (facts.isEmpty()) UiState.Empty else UiState.Success(facts)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BansifyTopBar(
                title = "Memory",
                onBack = onBack,
                actions = {
                    if (facts.isNotEmpty()) {
                        BansifyTextButton(
                            text = "Clear all",
                            onClick = { showClearDialog = true },
                            color = BansifyThemeTokens.extendedColors.danger,
                            testTag = "memory_clear_all"
                        )
                    }
                    IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Memory options")
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                load(simulateError = true)
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
                .testTag("memory_screen")
        ) {
            when (val current = loadState) {
                is UiState.Loading -> SkeletonList(count = 4)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load memory",
                    message = current.message,
                    onRetry = { load(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "No saved memories yet",
                    subtitle = "As you chat with Bansify, helpful preferences like your stack or writing style appear here — and you can delete them anytime."
                )
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Space.md),
                        verticalArrangement = Arrangement.spacedBy(Space.xs),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(current.data, key = { it.id }) { fact ->
                            Surface(
                                shape = BansifyRadius.Card,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                tonalElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(Space.md)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(Space.sm))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fact.category,
                                            style = BansifyTextStyles.CaptionStrong,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = fact.text,
                                            style = BansifyTextStyles.Body,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteMemoryFact(fact.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete memory fact",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Clear all memories?",
            message = "This removes all saved preference facts. Your chat history will remain untouched.",
            confirmLabel = "Clear all",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllMemory()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}
