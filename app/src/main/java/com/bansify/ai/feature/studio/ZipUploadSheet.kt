package com.bansify.ai.feature.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.core.ui.SheetScaffold
import kotlinx.coroutines.delay

private enum class ZipStage { PICK, PROGRESS, RESULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipUploadSheet(
    onDismiss: () -> Unit
) {
    var stage by remember { mutableStateOf(ZipStage.PICK) }
    var chosenFile by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var conflictChoice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(stage) {
        if (stage == ZipStage.PROGRESS) {
            progress = 0.1f
            repeat(9) { step ->
                delay(110L)
                progress = ((step + 2) / 10f).coerceAtMost(1f)
            }
            stage = ZipStage.RESULT
        }
    }

    SheetScaffold(
        onDismiss = onDismiss,
        title = "ZIP workspace upload (Demo)"
    ) {
        when (stage) {
            ZipStage.PICK -> {
                Surface(
                    shape = BansifyRadius.Card,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BansifyRadius.Card)
                        .clickable { chosenFile = "bansify-ai-patch.zip (2.4 MB)" }
                        .testTag("zip_drop_zone")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(Space.lg)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudUpload,
                            contentDescription = "Drop ZIP archive",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(Space.xs))
                        Text(
                            text = chosenFile ?: "Drop .zip archive here",
                            style = BansifyTextStyles.BodyStrong,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(Space.xxs))
                        Text(
                            text = "Local mock unpack — no files uploaded",
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(Space.sm))
                        TonalButton(
                            text = if (chosenFile == null) "Choose file (Demo)" else "Change file",
                            onClick = { chosenFile = "bansify-ai-patch.zip (2.4 MB)" },
                            testTag = "zip_choose_file"
                        )
                    }
                }
                Spacer(Modifier.height(Space.md))
                PrimaryButton(
                    text = "Unpack & sync",
                    enabled = chosenFile != null,
                    onClick = { stage = ZipStage.PROGRESS },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "zip_unpack_button"
                )
            }

            ZipStage.PROGRESS -> {
                Text(
                    text = "Unpacking ${chosenFile ?: "archive.zip"}…",
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Space.sm))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "${(progress * 100).toInt()}% · Checking conflicts",
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ZipStage.RESULT -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Unpacked",
                        tint = BansifyThemeTokens.extendedColors.success
                    )
                    Spacer(Modifier.width(Space.xs))
                    Text(
                        text = "42 files added · 3 updated · 1 conflict",
                        style = BansifyTextStyles.BodyStrong,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("zip_result_summary")
                    )
                }
                Spacer(Modifier.height(Space.md))
                Surface(
                    shape = BansifyRadius.Card,
                    color = BansifyThemeTokens.extendedColors.paidBadgeBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Space.sm)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = "Conflict",
                                tint = BansifyThemeTokens.extendedColors.paidBadgeFg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Space.xs))
                            Text(
                                text = "core/designsystem/Theme.kt",
                                style = BansifyTextStyles.CaptionStrong,
                                color = BansifyThemeTokens.extendedColors.paidBadgeFg
                            )
                        }
                        Spacer(Modifier.height(Space.xs))
                        if (conflictChoice != null) {
                            Text(
                                text = "Resolution selected: $conflictChoice (Demo)",
                                style = BansifyTextStyles.Caption,
                                color = BansifyThemeTokens.extendedColors.paidBadgeFg
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                                BansifyTextButton(
                                    text = "Keep",
                                    onClick = { conflictChoice = "Keep mine" },
                                    color = BansifyThemeTokens.extendedColors.paidBadgeFg
                                )
                                BansifyTextButton(
                                    text = "Theirs",
                                    onClick = { conflictChoice = "Use theirs" },
                                    color = BansifyThemeTokens.extendedColors.paidBadgeFg
                                )
                                BansifyTextButton(
                                    text = "Review",
                                    onClick = { conflictChoice = "Marked for review" },
                                    color = BansifyThemeTokens.extendedColors.paidBadgeFg
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Space.md))
                PrimaryButton(
                    text = "Done",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
