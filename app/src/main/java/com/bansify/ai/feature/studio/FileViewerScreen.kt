package com.bansify.ai.feature.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.EmptyState
import com.bansify.ai.core.ui.ErrorState
import com.bansify.ai.core.ui.SkeletonList
import com.bansify.ai.data.mock.RepoFileNode
import com.bansify.ai.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FileViewerScreen(
    repoId: String,
    filePath: String,
    viewModel: StudioViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<UiState<RepoFileNode>>(UiState.Loading) }
    var isEditingMock by remember { mutableStateOf(false) }
    var draftContent by remember { mutableStateOf("") }
    var showOverflow by remember { mutableStateOf(false) }

    fun loadFile(simulateError: Boolean = false) {
        scope.launch {
            state = UiState.Loading
            delay(620L)
            if (simulateError) {
                state = UiState.Error("File buffer read error (mock).")
                return@launch
            }
            val found = viewModel.getFile(repoId, filePath)
            if (found == null) {
                state = UiState.Empty
            } else {
                draftContent = found.content
                state = UiState.Success(found)
            }
        }
    }

    LaunchedEffect(repoId, filePath) { loadFile(false) }

    val fileNode = (state as? UiState.Success)?.data
    val isDirty = fileNode != null && isEditingMock && draftContent != fileNode.content

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BansifyTopBar(
                title = fileNode?.name ?: filePath.substringAfterLast('/'),
                onBack = onBack,
                actions = {
                    if (fileNode != null) {
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(draftContent))
                                scope.launch { snackbarHostState.showSnackbar("Copied entire file") }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("file_viewer_copy")
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy all code")
                        }
                    }
                    IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "File options")
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isEditingMock) "Exit edit mock" else "Edit (mock)",
                                    style = BansifyTextStyles.Body
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            onClick = {
                                showOverflow = false
                                isEditingMock = !isEditingMock
                                if (isEditingMock && fileNode != null && draftContent == fileNode.content) {
                                    draftContent = "${fileNode.content}\n// Edited in Bansify Studio (Demo)"
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulate error (Demo)", style = BansifyTextStyles.Body) },
                            onClick = {
                                showOverflow = false
                                loadFile(simulateError = true)
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
                .testTag("studio_file_viewer_screen")
        ) {
            when (val current = state) {
                is UiState.Loading -> SkeletonList(count = 3)
                is UiState.Error -> ErrorState(
                    title = "Couldn't open file",
                    message = current.message,
                    onRetry = { loadFile(false) }
                )
                is UiState.Empty -> EmptyState(
                    title = "File not found",
                    subtitle = "This path does not exist on the selected branch.",
                    actionLabel = "Go back",
                    onActionClick = onBack
                )
                is UiState.Success -> {
                    val file = current.data
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = Space.md, vertical = Space.xs)
                    ) {
                        Text(
                            text = file.path,
                            style = BansifyTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = BansifyRadius.Chip,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = file.language,
                                style = BansifyTextStyles.CaptionStrong,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isEditingMock) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BansifyThemeTokens.extendedColors.paidBadgeBg)
                                .padding(horizontal = Space.md, vertical = Space.xxs)
                                .testTag("unsaved_changes_banner")
                        ) {
                            Text(
                                text = if (isDirty) "Unsaved changes · Read-only mock" else "Edit preview (Demo)",
                                style = BansifyTextStyles.CaptionStrong,
                                color = BansifyThemeTokens.extendedColors.paidBadgeFg
                            )
                            Row {
                                BansifyTextButton(
                                    text = "Discard",
                                    onClick = {
                                        draftContent = file.content
                                        isEditingMock = false
                                    },
                                    color = BansifyThemeTokens.extendedColors.paidBadgeFg
                                )
                                BansifyTextButton(
                                    text = "Save (Demo)",
                                    onClick = {
                                        viewModel.saveMockFileEdit(repoId, file.path, draftContent)
                                        state = UiState.Success(file.copy(content = draftContent))
                                        isEditingMock = false
                                        scope.launch { snackbarHostState.showSnackbar("Demo: Saved locally in memory") }
                                    }
                                )
                            }
                        }
                    }

                    if (isEditingMock) {
                        OutlinedTextField(
                            value = draftContent,
                            onValueChange = { draftContent = it },
                            textStyle = BansifyTextStyles.Mono.copy(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Space.md)
                                .testTag("file_editor_field")
                        )
                    } else {
                        val lines = remember(file.content) { file.content.lines() }
                        val keywordColor = MaterialTheme.colorScheme.primary
                        val commentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        val stringColor = BansifyThemeTokens.extendedColors.success

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = Space.sm)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = Space.sm, vertical = Space.xs)
                            ) {
                                lines.indices.forEach { idx ->
                                    Text(
                                        text = "${idx + 1}",
                                        style = BansifyTextStyles.Mono,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = Space.sm, vertical = Space.xs)
                            ) {
                                lines.forEach { line ->
                                    Text(
                                        text = highlightCodeLine(line, keywordColor, commentColor, stringColor),
                                        style = BansifyTextStyles.Mono,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val codeKeywords = setOf(
    "package", "import", "class", "object", "fun", "val", "var", "return", "if", "else",
    "enum", "sealed", "interface", "data", "override", "export", "const", "function", "type", "struct", "func"
)

private fun highlightCodeLine(
    line: String,
    keywordColor: Color,
    commentColor: Color,
    stringColor: Color
): AnnotatedString = buildAnnotatedString {
    val trimmed = line.trimStart()
    if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("--")) {
        withStyle(SpanStyle(color = commentColor)) { append(line) }
        return@buildAnnotatedString
    }
    val tokenRegex = Regex("""("[^"]*"|\b[A-Za-z_][A-Za-z0-9_]*\b)""")
    var last = 0
    for (match in tokenRegex.findAll(line)) {
        if (match.range.first > last) {
            append(line.substring(last, match.range.first))
        }
        val token = match.value
        when {
            token.startsWith("\"") -> withStyle(SpanStyle(color = stringColor)) { append(token) }
            token in codeKeywords -> withStyle(SpanStyle(color = keywordColor)) { append(token) }
            else -> append(token)
        }
        last = match.range.last + 1
    }
    if (last < line.length) append(line.substring(last))
}
