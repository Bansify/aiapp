package com.bansify.ai.feature.account

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri
import com.bansify.ai.core.designsystem.BansifyRadius
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.core.ui.SheetScaffold
import com.bansify.ai.data.auth.AccountSession
import com.bansify.ai.data.auth.AccountsClient

/**
 * The shared account switcher. It mirrors the Gothwad account sheet used in Mail and Store:
 * one place to see every signed-in account, switch between them, add another, manage the
 * account in the browser, or sign out.
 */
@Composable
fun AccountSheet(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onAddAccount: () -> Unit,
    onOpenProviders: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SheetScaffold(onDismiss = onDismiss, title = "Accounts") {
        state.accounts.forEach { account ->
            AccountRow(
                account = account,
                isActive = account.userId == state.activeAccount?.userId,
                onClick = {
                    viewModel.switchTo(account.userId)
                    onDismiss()
                },
                onSignOut = { viewModel.signOut(account.userId) }
            )
        }
        if (state.accounts.isEmpty()) {
            Text(
                text = "You are using Bansify AI as a guest. Sign in to sync your account across the Gothwad apps.",
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Space.sm))
        }
        Spacer(Modifier.height(Space.sm))
        PrimaryButton(
            text = "Add another account",
            onClick = {
                onDismiss()
                onAddAccount()
            },
            leadingIcon = Icons.Rounded.PersonAdd,
            modifier = Modifier.fillMaxWidth(),
            testTag = "account_add"
        )
        Spacer(Modifier.height(Space.xs))
        SheetAction(
            icon = Icons.Rounded.ManageAccounts,
            title = "Manage your Gothwad account",
            subtitle = "Profile, password, 2-step verification and devices",
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, AccountsClient.ACCOUNTS_WEB_URL.toUri()))
            }
        )
        SheetAction(
            icon = Icons.Rounded.Key,
            title = "AI providers and keys",
            subtitle = "Add a free API key for Gemini, Groq, OpenRouter and more",
            onClick = {
                onDismiss()
                onOpenProviders()
            }
        )
        if (state.accounts.isNotEmpty()) {
            Spacer(Modifier.height(Space.xs))
            BansifyTextButton(
                text = "Sign out of all accounts",
                onClick = {
                    viewModel.signOutAll()
                    onDismiss()
                },
                testTag = "account_sign_out_all"
            )
        }
        Spacer(Modifier.height(Space.sm))
    }
}

/** Full-screen account control, opened from Settings. */
@Composable
fun AccountScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onOpenProviders: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BansifyTopBar(title = "Accounts", onBack = onBack) }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.sm),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Space.md)
                .testTag("account_screen")
        ) {
            if (state.accounts.isEmpty()) {
                Text(
                    text = "Guest mode",
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Chats stay on this device. Sign in to use the same Gothwad account as Mail, Store and Drive.",
                    style = BansifyTextStyles.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.accounts.forEach { account ->
                AccountRow(
                    account = account,
                    isActive = account.userId == state.activeAccount?.userId,
                    onClick = { viewModel.switchTo(account.userId) },
                    onSignOut = { viewModel.signOut(account.userId) }
                )
            }
            Spacer(Modifier.height(Space.xs))
            PrimaryButton(
                text = "Add another account",
                onClick = onAddAccount,
                leadingIcon = Icons.Rounded.PersonAdd,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                shape = BansifyRadius.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SheetAction(
                        icon = Icons.Rounded.ManageAccounts,
                        title = "Manage your Gothwad account",
                        subtitle = AccountsClient.ACCOUNTS_WEB_URL,
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, AccountsClient.ACCOUNTS_WEB_URL.toUri()))
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    SheetAction(
                        icon = Icons.Rounded.Key,
                        title = "AI providers and keys",
                        subtitle = "Free keys only, stored on this device",
                        onClick = onOpenProviders
                    )
                }
            }
            if (state.accounts.isNotEmpty()) {
                BansifyTextButton(
                    text = "Sign out of all accounts",
                    onClick = { viewModel.signOutAll() }
                )
            }
        }
    }
}

@Composable
private fun AccountRow(
    account: AccountSession,
    isActive: Boolean,
    onClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        shape = BansifyRadius.Card,
        color = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Space.md)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = account.initial,
                        style = BansifyTextStyles.BodyStrong,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.width(Space.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName,
                    style = BansifyTextStyles.BodyStrong,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = account.email.ifBlank { account.username },
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isActive) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Active account",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Logout,
                    contentDescription = "Sign out",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onSignOut)
                )
            }
        }
    }
}

@Composable
private fun SheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Space.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Space.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = BansifyTextStyles.BodyStrong, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = subtitle,
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
