package com.bansify.ai.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = BansifyRadius.Input,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.sm),
        modifier = modifier
            .heightIn(min = 52.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(Space.xs))
        }
        Text(
            text = text,
            style = BansifyTextStyles.BodyStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = BansifyRadius.Input,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.sm),
        modifier = modifier
            .heightIn(min = 52.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(Space.xs))
        }
        Text(
            text = text,
            style = BansifyTextStyles.BodyStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BansifyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    testTag: String? = null
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(text = text, style = BansifyTextStyles.BodyStrong)
    }
}

@Composable
fun IconPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String? = null
) {
    Surface(
        shape = BansifyRadius.Full,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(BansifyRadius.Full)
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xxs),
            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = BansifyTextStyles.CaptionStrong,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TextFieldOutlined(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 6,
    supportingText: String? = null,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    testTag: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        placeholder = {
            Text(
                text = placeholder,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = label?.let { { Text(it, style = BansifyTextStyles.Caption) } },
        supportingText = supportingText?.let { { Text(it, style = BansifyTextStyles.Caption) } },
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = BansifyRadius.Input,
        textStyle = BansifyTextStyles.Body,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    showCheckIcon: Boolean = true
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val selected = option in selectedOptions
            Surface(
                shape = BansifyRadius.Chip,
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(BansifyRadius.Chip)
                    .clickable(role = Role.Checkbox) { onToggle(option) }
                    .testTag("chip_${option.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xxs),
                    modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
                ) {
                    if (selected && showCheckIcon) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = option,
                        style = BansifyTextStyles.CaptionStrong,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

enum class HealthStatus { UP, SLOW, DOWN }

@Composable
fun StatusDot(
    status: HealthStatus,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp
) {
    val colors = BansifyThemeTokens.extendedColors
    val dotColor = when (status) {
        HealthStatus.UP -> colors.success
        HealthStatus.SLOW -> colors.warning
        HealthStatus.DOWN -> colors.danger
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(BansifyRadius.Full)
            .background(dotColor)
    )
}

@Composable
fun FreeBadge(
    isFree: Boolean,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val colors = BansifyThemeTokens.extendedColors
    val bg = if (isFree) colors.freeBadgeBg else colors.paidBadgeBg
    val fg = if (isFree) colors.freeBadgeFg else colors.paidBadgeFg
    val text = customLabel ?: if (isFree) "FREE" else "PAID"
    Box(
        modifier = modifier
            .clip(BansifyRadius.Chip)
            .background(bg)
            .padding(horizontal = Space.xs, vertical = Space.xxs)
    ) {
        Text(
            text = text,
            style = BansifyTextStyles.CaptionStrong.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.xs)
    ) {
        Text(
            text = title,
            style = BansifyTextStyles.CaptionStrong,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = BansifyTextStyles.CaptionStrong,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(BansifyRadius.Chip)
                    .clickable(role = Role.Button, onClick = onActionClick)
                    .padding(horizontal = Space.xs, vertical = Space.xxs)
            )
        }
    }
}

@Composable
fun BansifyBrandAvatar(
    size: Dp = 24.dp,
    initial: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(BansifyRadius.Full)
            .background(BansifyBrandGradient)
    ) {
        if (initial != null) {
            Text(
                text = initial.take(1).uppercase(),
                style = if (size >= 56.dp) BansifyTextStyles.H1 else BansifyTextStyles.BodyStrong,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Bansify AI",
                tint = Color.White,
                modifier = Modifier.size(size * 0.56f)
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = BansifyRadius.SheetFull,
        title = { Text(text = title, style = BansifyTextStyles.H2) },
        text = {
            Text(
                text = message,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isDestructive) {
                        BansifyThemeTokens.extendedColors.danger
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(text = confirmLabel, style = BansifyTextStyles.BodyStrong)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
