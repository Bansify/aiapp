package com.bansify.ai.feature.drawer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.data.mock.Conversation

@Composable
internal fun SidebarGridTile(
    icon: ImageVector,
    label: String,
    tag: String,
    highlighted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (highlighted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (highlighted) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    } else {
        null
    }

    Surface(
        shape = BansifyRadius.Card,
        color = containerColor,
        border = border,
        tonalElevation = 0.dp,
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(BansifyRadius.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.sm, vertical = Space.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(Space.xs))
            Text(
                text = label,
                style = BansifyTextStyles.BodyStrong,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun SidebarConversationRow(
    conv: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(BansifyRadius.Input)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = Space.sm)
    ) {
        Text(
            text = conv.title,
            style = if (isSelected) BansifyTextStyles.BodyStrong else BansifyTextStyles.Body,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Space.sm)
        )
        IconButton(
            onClick = onTogglePin,
            modifier = Modifier
                .size(48.dp)
                .testTag("drawer_pin_${conv.id}")
        ) {
            Icon(
                imageVector = if (conv.isPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (conv.isPinned) "Unpin conversation" else "Pin conversation",
                tint = if (conv.isPinned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun SidebarProjectRow(
    project: com.bansify.ai.data.mock.MockRepo,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(BansifyRadius.Input)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Space.sm, vertical = Space.xs)
            .testTag("drawer_project_${project.id}")
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(
                text = project.projectName,
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${project.fullName} · ${project.activeBranch} · ${project.connectedIntegrationIds.size} connected",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
