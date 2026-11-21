package com.bansify.ai.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.ChipGroup
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.data.mock.MockUser

@Composable
fun GoalsScreen(
    selectedGoals: Set<String>,
    isSaving: Boolean,
    onToggleGoal: (String) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BansifyTopBar(
                title = "",
                onBack = onBack,
                actions = {
                    BansifyTextButton(
                        text = "Skip",
                        onClick = onSkip,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        testTag = "goals_top_skip"
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Space.lg, vertical = Space.md)
                .widthIn(max = 600.dp)
                .testTag("onboarding_goals_screen")
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Step 3 of 3 · Optional",
                        style = BansifyTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${selectedGoals.size}/3",
                        style = BansifyTextStyles.CaptionStrong,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("goals_counter")
                    )
                }
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "What can I help you with?",
                    style = BansifyTextStyles.H1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "Pick up to 3 topics.",
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Space.lg))
                ChipGroup(
                    options = MockUser.goalOptions,
                    selectedOptions = selectedGoals,
                    onToggle = onToggleGoal,
                    showCheckIcon = true
                )
                Spacer(Modifier.height(Space.lg))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = if (isSaving) "Getting ready…" else "Get started",
                    onClick = onStart,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "goals_start_button"
                )
                Spacer(Modifier.height(Space.xs))
                BansifyTextButton(
                    text = "Skip",
                    onClick = onSkip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "goals_bottom_skip"
                )
            }
        }
    }
}
