package com.bansify.ai.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.ThemeMode
import com.bansify.ai.core.ui.BansifyTopBar

@Composable
fun AppearanceScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BansifyTopBar(title = "Appearance", onBack = onBack) }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Space.md)
                .testTag("appearance_screen")
        ) {
            SectionHeader(title = "Theme")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val modes = listOf(
                    ThemeMode.SYSTEM to "System",
                    ThemeMode.LIGHT to "Light",
                    ThemeMode.DARK to "Dark"
                )
                modes.forEachIndexed { idx, (mode, label) ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(idx, modes.size),
                        modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                    ) {
                        Text(label, style = BansifyTextStyles.CaptionStrong, maxLines = 1)
                    }
                }
            }

            SectionHeader(title = "Font size preview")
            SettingsCard {
                Column(modifier = Modifier.padding(Space.md)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Text scale", style = BansifyTextStyles.BodyStrong)
                        Text("${(settings.fontScale * 100).toInt()}%", style = BansifyTextStyles.CaptionStrong, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.fontScale,
                        onValueChange = viewModel::setFontScale,
                        valueRange = 0.85f..1.4f,
                        modifier = Modifier.testTag("font_scale_slider")
                    )
                    Spacer(Modifier.height(Space.xs))
                    Surface(
                        shape = BansifyRadius.Card,
                        color = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Space.sm)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.xs)
                            ) {
                                BansifyBrandAvatar(size = 20.dp)
                                Text("Bansify AI", style = BansifyTextStyles.CaptionStrong)
                            }
                            Spacer(Modifier.height(Space.xxs))
                            Text(
                                text = "Hello! This is a live font size preview — text stays crisp and legible even at 200% system scale.",
                                style = BansifyTextStyles.Body,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsToggleRow(
                    title = "Dynamic colour",
                    subtitle = "Android 12+ wallpaper accent blend (optional)",
                    checked = settings.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor
                )
            }
        }
    }
}
