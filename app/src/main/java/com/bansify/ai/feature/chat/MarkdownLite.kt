package com.bansify.ai.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.BansifyThemeTokens
import com.bansify.ai.core.designsystem.JetBrainsMonoFontFamily
import com.bansify.ai.core.designsystem.Space
import kotlinx.coroutines.delay

private sealed interface MdBlock {
    data class Heading(val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class BulletItem(val marker: String, val text: String) : MdBlock
    data class CodeFence(val language: String, val code: String) : MdBlock
}

@Composable
fun MarkdownLiteText(
    markdown: String,
    isStreaming: Boolean = false,
    onCopyCode: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isStreaming) {
        if (isStreaming) {
            while (true) {
                cursorVisible = !cursorVisible
                delay(420L)
            }
        } else {
            cursorVisible = false
        }
    }

    val codeInlineBg = MaterialTheme.colorScheme.surfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = modifier.fillMaxWidth()
    ) {
        if (blocks.isEmpty() && isStreaming) {
            Text(
                text = if (cursorVisible) "▍" else " ",
                style = BansifyTextStyles.EditorialBody,
                color = accent
            )
        }
        blocks.forEachIndexed { idx, block ->
            val appendCursor = isStreaming && idx == blocks.lastIndex && cursorVisible
            when (block) {
                is MdBlock.Heading -> {
                    Spacer(Modifier.height(Space.xxs))
                    Text(
                        text = buildInlineMarkdown(
                            text = if (appendCursor) "${block.text} ▍" else block.text,
                            inlineCodeBg = codeInlineBg,
                            linkColor = accent
                        ),
                        style = BansifyTextStyles.EditorialHeading,
                        color = textColor
                    )
                }

                is MdBlock.Paragraph -> {
                    Text(
                        text = buildInlineMarkdown(
                            text = if (appendCursor) "${block.text} ▍" else block.text,
                            inlineCodeBg = codeInlineBg,
                            linkColor = accent
                        ),
                        style = BansifyTextStyles.EditorialBody,
                        color = textColor
                    )
                }

                is MdBlock.BulletItem -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = block.marker,
                            style = BansifyTextStyles.EditorialBody,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(Space.lg)
                        )
                        Text(
                            text = buildInlineMarkdown(
                                text = if (appendCursor) "${block.text} ▍" else block.text,
                                inlineCodeBg = codeInlineBg,
                                linkColor = accent
                            ),
                            style = BansifyTextStyles.EditorialBody,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MdBlock.CodeFence -> {
                    CodeBlock(
                        language = block.language.ifBlank { "code" },
                        code = if (appendCursor) "${block.code}▍" else block.code,
                        onCopy = { onCopyCode(block.code) }
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlock(
    language: String,
    code: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = BansifyThemeTokens.extendedColors
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500L)
            copied = false
        }
    }

    Surface(
        shape = BansifyRadius.Card,
        color = extended.codeBlockBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(extended.codeBlockHeaderBg)
                    .padding(start = Space.md, end = Space.xxs, top = Space.xxs, bottom = Space.xxs)
            ) {
                Text(
                    text = language.lowercase(),
                    style = BansifyTextStyles.CaptionStrong,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(code))
                        copied = true
                        onCopy()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = if (copied) "Copied code" else "Copy code",
                        tint = if (copied) extended.success else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = code,
                style = BansifyTextStyles.Mono,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(Space.md)
            )
        }
    }
}

private fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    if (raw.isBlank()) return emptyList()
    val lines = raw.lines()
    val result = mutableListOf<MdBlock>()
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()
        when {
            trimmed.startsWith("```") -> {
                val lang = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines += lines[i]
                    i++
                }
                result += MdBlock.CodeFence(lang, codeLines.joinToString("\n"))
            }
            trimmed.startsWith("### ") -> {
                result += MdBlock.Heading(trimmed.removePrefix("### ").trim())
            }
            trimmed.startsWith("## ") -> {
                result += MdBlock.Heading(trimmed.removePrefix("## ").trim())
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                result += MdBlock.BulletItem("•", trimmed.drop(2).trim())
            }
            Regex("^\\d+\\.\\s+.*").matches(trimmed) -> {
                val dot = trimmed.indexOf('.')
                val marker = trimmed.substring(0, dot + 1)
                val body = trimmed.substring(dot + 1).trim()
                result += MdBlock.BulletItem(marker, body)
            }
            trimmed.isNotEmpty() -> {
                result += MdBlock.Paragraph(trimmed)
            }
        }
        i++
    }
    return result
}

private fun buildInlineMarkdown(
    text: String,
    inlineCodeBg: Color,
    linkColor: Color
): AnnotatedString = buildAnnotatedString {
    var i = 0
    val len = text.length
    while (i < len) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("**")
                    i += 2
                }
            }
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            background = inlineCodeBg
                        )
                    ) {
                        append(" ${text.substring(i + 1, end)} ")
                    }
                    i = end + 1
                } else {
                    append('`')
                    i++
                }
            }
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end != -1 && end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('*')
                    i++
                }
            }
            text.startsWith("https://", i) || text.startsWith("http://", i) -> {
                var end = i
                while (end < len && !text[end].isWhitespace()) end++
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(text.substring(i, end))
                }
                i = end
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
