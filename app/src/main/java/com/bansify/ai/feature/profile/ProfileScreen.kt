package com.bansify.ai.feature.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.FreeBadge
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TonalButton
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.feature.settings.SettingsViewModel

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onEditPersona: () -> Unit,
    onOpenAccount: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()

    val displayName = if (profile.isGuest || profile.name.isBlank()) "Guest" else profile.name
    val totalMessages = conversations.sumOf { it.messages.size }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BansifyTopBar(title = "Profile", onBack = onBack) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Space.md)
                .testTag("profile_screen")
        ) {
            Spacer(Modifier.height(Space.xs))
            BansifyBrandAvatar(size = 80.dp, initial = displayName)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayName,
                    style = BansifyTextStyles.H1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = profile.profession.ifBlank { "Persona not set yet" },
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TonalButton(
                text = "Edit persona",
                leadingIcon = Icons.Rounded.Edit,
                onClick = onEditPersona,
                testTag = "profile_edit_persona"
            )

            // Stats row (mock)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatTile("Chats", "${conversations.size}", Modifier.weight(1f))
                StatTile("Messages", "$totalMessages", Modifier.weight(1f))
                StatTile("Pinned", "${conversations.count { it.isPinned }}", Modifier.weight(1f))
                StatTile(
                    "Last model",
                    conversations.firstOrNull()?.modelLabel?.take(12) ?: "Auto",
                    Modifier.weight(1.2f)
                )
            }

            // Account card
            Surface(
                shape = BansifyRadius.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Space.md)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (profile.isGuest) "Guest mode" else "Gothwad account",
                                style = BansifyTextStyles.BodyStrong,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (profile.isGuest) {
                                    "Chats stay temporarily on this device only."
                                } else {
                                    profile.email
                                },
                                style = BansifyTextStyles.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FreeBadge(isFree = true, customLabel = if (profile.isGuest) "Guest" else "Free")
                    }

                    Spacer(Modifier.height(Space.md))
                    if (profile.isGuest) {
                        PrimaryButton(
                            text = "Sign in with your Gothwad account",
                            onClick = onOpenAccount,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "profile_sign_in_button"
                        )
                    } else {
                        TonalButton(
                            text = "Manage accounts",
                            onClick = onOpenAccount,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "profile_sign_out_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = BansifyRadius.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.sm)
        ) {
            Text(
                text = value,
                style = BansifyTextStyles.BodyStrong,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
