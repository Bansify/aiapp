package com.bansify.ai.feature.workspace

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.ChipGroup
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ScheduledTaskItem(
    val id: String,
    val title: String,
    val prompt: String,
    val cadence: String,
    val enabled: Boolean
)

private val initialTasks = listOf(
    ScheduledTaskItem(
        id = "sch_1",
        title = "Morning Study Brief",
        prompt = "Summarize 3 key DSA patterns and give one quick practice question.",
        cadence = "Daily · 8:00 AM",
        enabled = true
    ),
    ScheduledTaskItem(
        id = "sch_2",
        title = "Android & Kotlin Weekly Digest",
        prompt = "Recap the most useful Jetpack Compose performance tips for the week.",
        cadence = "Mondays · 9:30 AM",
        enabled = true
    ),
    ScheduledTaskItem(
        id = "sch_3",
        title = "Evening Focus Wrap-Up",
        prompt = "Ask me what I shipped today and organize tomorrow's top 3 priorities.",
        cadence = "Weekdays · 9:00 PM",
        enabled = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tasks by remember { mutableStateOf(initialTasks) }
    var state by remember { mutableStateOf<UiState<Unit>>(UiState.Loading) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    fun load(simulateError: Boolean = false) {
        scope.launch {
            state = UiState.Loading
            delay(620L)
            state = when {
                simulateError -> UiState.Error("Unable to load scheduled actions (mock error).")
                tasks.isEmpty() -> UiState.Empty
                else -> UiState.Success(Unit)
            }
        }
    }

    LaunchedEffect(Unit) { load(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BansifyTopBar(
                title = "Scheduled",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { showCreateSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("scheduled_add_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Create scheduled task")
                    }
                    IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Scheduled options")
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh (Loading demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                load(false)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                load(true)
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
                .testTag("scheduled_screen")
        ) {
            when (val current = state) {
                is UiState.Loading -> SkeletonList(count = 3)
                is UiState.Error -> ErrorState(
                    title = "Couldn't load schedules",
                    message = current.message,
                    onRetry = { load(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "No scheduled tasks yet",
                    subtitle = "Automate recurring briefings, study check-ins, or reminders.",
                    actionLabel = "Create schedule",
                    onActionClick = { showCreateSheet = true }
                )
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Space.md),
                        verticalArrangement = Arrangement.spacedBy(Space.sm),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tasks, key = { it.id }) { task ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(Space.xxs))
                                            Text(
                                                text = task.cadence,
                                                style = BansifyTextStyles.CaptionStrong,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(Modifier.height(Space.xxs))
                                        Text(
                                            text = task.title,
                                            style = BansifyTextStyles.BodyStrong,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(Space.xxs))
                                        Text(
                                            text = task.prompt,
                                            style = BansifyTextStyles.Caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.width(Space.sm))
                                    Switch(
                                        checked = task.enabled,
                                        onCheckedChange = { next ->
                                            tasks = tasks.map {
                                                if (it.id == task.id) it.copy(enabled = next) else it
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        var titleDraft by remember { mutableStateOf("") }
        var promptDraft by remember { mutableStateOf("") }
        var cadence by remember { mutableStateOf("Daily · 8:00 AM") }
        val cadenceOptions = listOf("Daily · 8:00 AM", "Weekdays · 6:00 PM", "Weekly · Sunday")

        SheetScaffold(
            onDismiss = { showCreateSheet = false },
            title = "New scheduled task (Demo)"
        ) {
            TextFieldOutlined(
                value = titleDraft,
                onValueChange = { titleDraft = it.take(48) },
                label = "Title",
                placeholder = "e.g. Daily Quiz Prep"
            )
            Spacer(Modifier.height(Space.xs))
            TextFieldOutlined(
                value = promptDraft,
                onValueChange = { promptDraft = it },
                label = "Prompt",
                placeholder = "What should Bansify run on schedule?",
                singleLine = false,
                maxLines = 3
            )
            Spacer(Modifier.height(Space.sm))
            ChipGroup(
                options = cadenceOptions,
                selectedOptions = setOf(cadence),
                onToggle = { cadence = it }
            )
            Spacer(Modifier.height(Space.md))
            PrimaryButton(
                text = "Save schedule (Demo)",
                enabled = titleDraft.isNotBlank() && promptDraft.isNotBlank(),
                onClick = {
                    val created = ScheduledTaskItem(
                        id = "sch_${System.currentTimeMillis()}",
                        title = titleDraft.trim(),
                        prompt = promptDraft.trim(),
                        cadence = cadence,
                        enabled = true
                    )
                    tasks = listOf(created) + tasks
                    state = UiState.Success(Unit)
                    showCreateSheet = false
                    scope.launch { snackbarHostState.showSnackbar("Scheduled task saved locally") }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
