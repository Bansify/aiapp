package com.bansify.ai.feature.settings

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.FreeBadge
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.data.mock.ResponseStyle

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenPersona: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenModels: () -> Unit,
    onOpenData: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenAccount: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val memoryFacts by viewModel.memoryFacts.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BansifyTopBar(title = "Settings", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen")
        ) {
            // ACCOUNT
            item(key = "sec_account") {
                SectionHeader(title = "Account")
                SettingsCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenProfile)
                            .padding(Space.md)
                    ) {
                        val labelName = if (profile.isGuest || profile.name.isBlank()) "Guest" else profile.name
                        BansifyBrandAvatar(size = 44.dp, initial = labelName)
                        Spacer(Modifier.width(Space.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = labelName, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = if (profile.isGuest) "Guest mode · local only" else profile.email,
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BansifyTextButton(
                            text = if (profile.isGuest) "Sign in" else "Accounts",
                            onClick = onOpenAccount,
                            testTag = "settings_auth_toggle"
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.sm)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Plan", style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
                            Text("Free · upgrade later", style = BansifyTextStyles.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FreeBadge(isFree = false, customLabel = "Soon")
                    }
                }
            }

            // PERSONALISATION
            item(key = "sec_personal") {
                SectionHeader(title = "Personalisation")
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Rounded.ColorLens,
                        title = "Appearance",
                        subtitle = "${settings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() }} · ${(settings.fontScale * 100).toInt()}% text",
                        tag = "settings_appearance_row",
                        onClick = onOpenAppearance
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsNavRow(
                        icon = Icons.Rounded.Person,
                        title = "Persona",
                        subtitle = listOfNotNull(
                            profile.name.takeIf { it.isNotBlank() },
                            profile.profession.takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifEmpty { "Set your name & profession" },
                        tag = "settings_persona_row",
                        onClick = onOpenPersona
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsNavRow(
                        icon = Icons.Rounded.Memory,
                        title = "Memory",
                        subtitle = "${memoryFacts.size} saved facts",
                        tag = "settings_memory_row",
                        onClick = onOpenMemory
                    )
                }
            }

            // MODELS
            item(key = "sec_models") {
                SectionHeader(title = "Models")
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Default model",
                        subtitle = viewModel.modelNameFor(settings.defaultModelId),
                        tag = "settings_default_model_row",
                        onClick = onOpenModels
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsToggleRow(
                        title = "Auto fallback",
                        subtitle = "Automatically switch to the next healthy free model when a limit is reached",
                        checked = settings.autoFallback,
                        onCheckedChange = viewModel::setAutoFallback
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsNavRow(
                        icon = Icons.Rounded.Tune,
                        title = "AI providers & keys",
                        subtitle = "Free providers · add your own key",
                        tag = "settings_provider_health_row",
                        onClick = onOpenProviders
                    )
                }
            }

            // CHAT
            item(key = "sec_chat") {
                SectionHeader(title = "Chat")
                SettingsCard {
                    Column(modifier = Modifier.padding(Space.md)) {
                        Text(text = "Response style", style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(Space.xs))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ResponseStyle.entries.forEachIndexed { idx, style ->
                                SegmentedButton(
                                    selected = settings.responseStyle == style,
                                    onClick = { viewModel.setResponseStyle(style) },
                                    shape = SegmentedButtonDefaults.itemShape(idx, ResponseStyle.entries.size)
                                ) {
                                    Text(style.label, style = BansifyTextStyles.CaptionStrong, maxLines = 1)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsToggleRow(
                        title = "Streaming responses",
                        subtitle = "Token-by-token live output with cursor",
                        checked = settings.streamingEnabled,
                        onCheckedChange = viewModel::setStreamingEnabled
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsToggleRow(
                        title = "Send on Enter",
                        subtitle = "Press the keyboard Enter key to send messages",
                        checked = settings.sendOnEnter,
                        onCheckedChange = viewModel::setSendOnEnter
                    )
                }
            }

            // DATA & ABOUT
            item(key = "sec_data_about") {
                SectionHeader(title = "Data & About")
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Rounded.Storage,
                        title = "Data controls",
                        subtitle = "Export, import, and delete all data",
                        tag = "settings_data_row",
                        onClick = onOpenData
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SettingsNavRow(
                        icon = Icons.Rounded.Info,
                        title = "About Bansify AI",
                        subtitle = "v1.0.0-proto · Made in India 🇮🇳",
                        tag = "settings_about_row",
                        onClick = onOpenAbout
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
internal fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(BansifyRadius.Input)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.sm)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Space.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = BansifyTextStyles.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = Space.md, vertical = Space.sm)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = BansifyTextStyles.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(Space.sm))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
