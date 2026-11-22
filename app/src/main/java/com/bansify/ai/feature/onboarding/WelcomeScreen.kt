package com.bansify.ai.feature.onboarding

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.core.ui.SheetScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onContinueWithGoogle: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSkip: () -> Unit
) {
    var legalSheetTopic by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Space.lg, vertical = Space.md)
                .widthIn(max = 600.dp)
                .testTag("onboarding_welcome_screen")
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                BansifyTextButton(
                    text = "Skip",
                    onClick = onSkip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "welcome_skip_button"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                BansifyBrandAvatar(size = 80.dp)
                Spacer(Modifier.height(Space.lg))
                Text(
                    text = "Bansify AI",
                    style = BansifyTextStyles.Splash,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "Thoughtful, fast, yours.",
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            PrimaryButton(
                text = "G   Continue with Google",
                onClick = onContinueWithGoogle,
                modifier = Modifier.fillMaxWidth(),
                testTag = "welcome_google_button"
            )
            Spacer(Modifier.height(Space.sm))
            TonalButton(
                text = "Continue as Guest",
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth(),
                testTag = "welcome_guest_button"
            )
            Spacer(Modifier.height(Space.md))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "By continuing, you agree to our ",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Terms",
                    style = BansifyTextStyles.Caption.copy(textDecoration = TextDecoration.Underline),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { legalSheetTopic = "Terms of Service" }
                        .padding(horizontal = Space.xxs, vertical = Space.xs)
                        .testTag("welcome_terms_link")
                )
                Text(
                    text = " and ",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Privacy",
                    style = BansifyTextStyles.Caption.copy(textDecoration = TextDecoration.Underline),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { legalSheetTopic = "Privacy Policy" }
                        .padding(horizontal = Space.xxs, vertical = Space.xs)
                        .testTag("welcome_privacy_link")
                )
                Text(
                    text = ".",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Space.xs))
        }
    }

    legalSheetTopic?.let { topic ->
        SheetScaffold(
            onDismiss = { legalSheetTopic = null },
            title = topic
        ) {
            Text(
                text = topic,
                style = BansifyTextStyles.BodyStrong,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                text = if (topic == "Terms of Service") {
                    "Bansify AI sends the messages you type to the AI provider you choose, using the API key you saved on this device. Chat history stays on this device. Sign-in uses your Gothwad account."
                } else {
                    "Your API keys and chat history stay on this device. Only the message you send goes to the provider you selected. There are no ads and no analytics. You can delete all chats from Settings at any time."
                },
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Space.lg))
            PrimaryButton(
                text = "Got it",
                onClick = { legalSheetTopic = null },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
