package com.bansify.ai.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar

@Composable
fun NameScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val isValid = name.trim().length in 1..40

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

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
                        testTag = "name_top_skip"
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
                .testTag("onboarding_name_screen")
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Step 1 of 3",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "What's your name?",
                    style = BansifyTextStyles.H1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.lg))
                TextFieldOutlined(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = "e.g. Pawan",
                    singleLine = true,
                    supportingText = "${name.length}/40",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { if (isValid) onContinue() }
                    ),
                    modifier = Modifier.focusRequester(focusRequester),
                    testTag = "name_input_field"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "name_continue_button"
                )
                Spacer(Modifier.height(Space.xs))
                Row {
                    BansifyTextButton(
                        text = "Skip",
                        onClick = onSkip,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        testTag = "name_bottom_skip"
                    )
                }
            }
        }
    }
}
