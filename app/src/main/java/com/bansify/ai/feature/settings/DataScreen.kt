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
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.ConfirmDialog
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.core.ui.BansifyTopBar
import kotlinx.coroutines.launch

@Composable
fun DataScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showFirstConfirm by remember { mutableStateOf(false) }
    var showTypeDeleteConfirm by remember { mutableStateOf(false) }
    var deleteKeyword by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { BansifyTopBar(title = "Data controls", onBack = onBack) }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Space.md)
                .testTag("data_screen")
        ) {
            SectionHeader(title = "Export & Import (Demo)")
            SettingsCard {
                SettingsNavRow(
                    icon = Icons.Rounded.Code,
                    title = "Export as JSON (Demo)",
                    subtitle = "Local mock export preview — no file written",
                    tag = "data_export_json",
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Demo: JSON archive schema ready") }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsNavRow(
                    icon = Icons.Rounded.Description,
                    title = "Export as Markdown (Demo)",
                    subtitle = "Clean readable .md format for all chats",
                    tag = "data_export_md",
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Demo: Markdown export preview ready") }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SettingsNavRow(
                    icon = Icons.Rounded.UploadFile,
                    title = "Import backup (Soon)",
                    subtitle = "Restore previous Bansify JSON backup",
                    tag = "data_import",
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Soon: Local backup import") }
                    }
                )
            }

            SectionHeader(title = "Danger zone")
            SettingsCard {
                Column(modifier = Modifier.padding(Space.md)) {
                    Text(
                        text = "Delete all local chats & memory",
                        style = BansifyTextStyles.BodyStrong,
                        color = BansifyThemeTokens.extendedColors.danger
                    )
                    Spacer(Modifier.height(Space.xxs))
                    Text(
                        text = "This permanently removes all conversations and saved memory facts from this device.",
                        style = BansifyTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Space.sm))
                    TonalButton(
                        text = "Delete all data",
                        leadingIcon = Icons.Rounded.DeleteForever,
                        onClick = { showFirstConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "data_delete_all_button"
                    )
                }
            }
        }
    }

    if (showFirstConfirm) {
        ConfirmDialog(
            title = "Delete all local data?",
            message = "Step 1 of 2: All mock chats and saved memories will be cleared. Do you want to continue?",
            confirmLabel = "Continue",
            isDestructive = true,
            onConfirm = {
                showFirstConfirm = false
                deleteKeyword = ""
                showTypeDeleteConfirm = true
            },
            onDismiss = { showFirstConfirm = false }
        )
    }

    if (showTypeDeleteConfirm) {
        val matches = deleteKeyword.trim() == "DELETE"
        AlertDialog(
            onDismissRequest = { showTypeDeleteConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = BansifyRadius.SheetFull,
            title = { Text("Type DELETE to confirm", style = BansifyTextStyles.H2) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Text(
                        text = "Step 2 of 2: Enter the exact word DELETE below.",
                        style = BansifyTextStyles.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextFieldOutlined(
                        value = deleteKeyword,
                        onValueChange = { deleteKeyword = it },
                        placeholder = "DELETE",
                        testTag = "confirm_delete_word_input"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = matches,
                    onClick = {
                        viewModel.deleteAllUserData()
                        showTypeDeleteConfirm = false
                        scope.launch { snackbarHostState.showSnackbar("All local chats & memory deleted") }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BansifyThemeTokens.extendedColors.danger
                    )
                ) {
                    Text("Delete forever", style = BansifyTextStyles.BodyStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTypeDeleteConfirm = false }) {
                    Text("Cancel", style = BansifyTextStyles.BodyStrong)
                }
            }
        )
    }
}
