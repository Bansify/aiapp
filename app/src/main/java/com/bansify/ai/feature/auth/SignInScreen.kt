package com.bansify.ai.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.BansifyThemeTokens

/**
 * The one gate in front of Bansify AI.
 *
 * Sign-in is the shared Bansify Accounts flow: the same account that opens Mail,
 * Drive and Store. The app never asks for a password here — the SDK opens
 * accounts.bansify.com in a Custom Tab and comes back with a PKCE code.
 */
@Composable
fun SignInScreen(
    onSignIn: () -> Unit,
    onSignInWithDifferentAccount: () -> Unit,
    isInitializing: Boolean = false,
    errorMessage: String? = null,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(Space.md))
            Text(
                text = "Bansify AI",
                style = BansifyTextStyles.H1,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.sm))
            Text(
                text = "Apne Bansify Account se sign in karo — wahi account jo Mail, Drive aur Store me chalta hai. Chat history account ke saath sync rehti hai.",
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(Space.md))
                Text(
                    text = errorMessage,
                    style = BansifyTextStyles.Caption,
                    color = BansifyThemeTokens.extendedColors.danger,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(Space.lg))

            if (isInitializing) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                PrimaryButton(
                    text = "Continue with Bansify Account",
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "sign_in_primary",
                )
                Spacer(Modifier.height(Space.sm))
                PrimaryButton(
                    text = "Choose another account",
                    onClick = onSignInWithDifferentAccount,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "sign_in_choose_account",
                )
            }

            Spacer(Modifier.height(Space.lg))
            Text(
                text = "Made in India",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
