package com.bansify.ai.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.SheetScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    var activeSheet by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BansifyTopBar(title = "About", onBack = onBack) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Space.md)
                .testTag("about_screen")
        ) {
            Spacer(Modifier.height(Space.xs))
            BansifyBrandAvatar(size = 64.dp)
            Text(text = "Bansify AI", style = BansifyTextStyles.H1, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "Version 1.0.0-proto · Made in India 🇮🇳",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SettingsCard {
                SettingsNavRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "What's new",
                    subtitle = "v1.0.0 prototype highlights",
                    tag = "about_whats_new",
                    onClick = {
                        activeSheet = "What's new in 1.0.0-proto" to
                            "• Calm Gemini-inspired chat with full-width Markdown & blinking cursor streaming\n• 15 model providers (9 card-free + 6 paid) with shared catalog\n• Developer Studio with repo tree, syntax-highlighted FileViewer & ZIP unpack preview\n• Light + Dark themes, font scaling, and DataStore persona"
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsNavRow(
                    icon = Icons.Rounded.Gavel,
                    title = "Open-source licences",
                    subtitle = "Jetpack Compose, Material 3, Inter, JetBrains Mono",
                    tag = "about_licences",
                    onClick = {
                        activeSheet = "Open-source licences" to
                            "Built with AndroidX Jetpack Compose, Material Design 3, Navigation Compose, Preferences DataStore, Kotlin Coroutines, Inter (SIL OFL), and JetBrains Mono (SIL OFL)."
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsNavRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy policy",
                    subtitle = "Zero tracking, offline UI prototype",
                    tag = "about_privacy",
                    onClick = {
                        activeSheet = "Privacy policy" to
                            "The Bansify AI prototype makes zero network calls. There are no ads, analytics, or third-party telemetry."
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsNavRow(
                    icon = Icons.Rounded.Description,
                    title = "Terms of service",
                    subtitle = "Usage guidelines (Demo)",
                    tag = "about_terms",
                    onClick = {
                        activeSheet = "Terms of service" to
                            "This UI prototype is built for product design review and interaction testing. Final Terms will be published alongside backend launch."
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsNavRow(
                    icon = Icons.Rounded.Email,
                    title = "Contact",
                    subtitle = "hello@bansify.ai (Demo)",
                    tag = "about_contact",
                    onClick = {
                        activeSheet = "Contact Bansify" to
                            "For feedback or bug reports: hello@bansify.ai (Demo — email client launch is disabled in offline prototype mode)."
                    }
                )
            }
        }
    }

    activeSheet?.let { (title, body) ->
        SheetScaffold(onDismiss = { activeSheet = null }, title = title) {
            Text(
                text = body,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Space.lg))
            PrimaryButton(
                text = "Close",
                onClick = { activeSheet = null },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
