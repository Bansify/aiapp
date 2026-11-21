package com.bansify.ai.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.ChipGroup
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.data.mock.MockUser

@Composable
fun ProfessionScreen(
    professionText: String,
    selectedChip: String?,
    onTextTyped: (String) -> Unit,
    onChipTapped: (String) -> Unit,
    onContinue: () -> Unit,
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
                        testTag = "profession_skip_button"
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = Space.lg, vertical = Space.md)
                .widthIn(max = 600.dp)
                .testTag("onboarding_profession_screen")
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Step 2 of 3",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "What do you do?",
                    style = BansifyTextStyles.H1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "This helps tailor answers to your work.",
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Space.lg))
                TextFieldOutlined(
                    value = professionText,
                    onValueChange = onTextTyped,
                    placeholder = "Type your role or pick one below",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { onContinue() }),
                    testTag = "profession_input_field"
                )
                Spacer(Modifier.height(Space.md))
                ChipGroup(
                    options = MockUser.professionOptions,
                    selectedOptions = selectedChip?.let { setOf(it) } ?: emptySet(),
                    onToggle = onChipTapped,
                    showCheckIcon = true
                )
                Spacer(Modifier.height(Space.lg))
            }

            PrimaryButton(
                text = "Continue",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                testTag = "profession_continue_button"
            )
        }
    }
}
