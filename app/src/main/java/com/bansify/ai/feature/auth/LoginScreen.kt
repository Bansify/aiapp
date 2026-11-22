package com.bansify.ai.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyBrandAvatar
import com.bansify.ai.core.designsystem.BansifyTextButton
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.designsystem.TonalButton

/**
 * Shared Gothwad sign-in sheet: the same account, sessions and sign-out as Mail, Store and Drive.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.signedIn) {
        if (state.signedIn) onSignedIn()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.lg, vertical = Space.md)
                .widthIn(max = 560.dp)
                .testTag("login_screen")
        ) {
            Spacer(Modifier.height(Space.lg))
            BansifyBrandAvatar(size = 72.dp)
            Spacer(Modifier.height(Space.md))
            Text(
                text = when (state.step) {
                    LoginStep.SIGN_UP -> "Create your Gothwad account"
                    LoginStep.MFA -> "Two-step verification"
                    LoginStep.FORGOT -> "Reset your password"
                    LoginStep.IDENTIFIER -> "Sign in to Bansify AI"
                },
                style = BansifyTextStyles.H1,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.xxs))
            Text(
                text = when (state.step) {
                    LoginStep.SIGN_UP -> "One account for Mail, Store, Drive and Bansify AI."
                    LoginStep.MFA -> "Enter the 6-digit code from your authenticator app, or a recovery code."
                    LoginStep.FORGOT -> "We will send a reset link to your recovery email."
                    LoginStep.IDENTIFIER -> "Use your Gothwad username, email or phone number."
                },
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.lg))

            state.notice?.let { notice ->
                Text(
                    text = notice,
                    style = BansifyTextStyles.Caption,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Space.sm))
            }

            when (state.step) {
                LoginStep.IDENTIFIER -> {
                    TextFieldOutlined(
                        value = state.identifier,
                        onValueChange = viewModel::onIdentifierChange,
                        placeholder = "username or you@gothwadtech.com",
                        label = "Account",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        testTag = "login_identifier"
                    )
                    Spacer(Modifier.height(Space.sm))
                    TextFieldOutlined(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Your password",
                        label = "Password",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        isError = state.error != null,
                        supportingText = state.error,
                        testTag = "login_password"
                    )
                    Spacer(Modifier.height(Space.md))
                    PrimaryButton(
                        text = if (state.isBusy) "Signing in…" else "Sign in",
                        onClick = viewModel::signIn,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_submit"
                    )
                    Spacer(Modifier.height(Space.xs))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BansifyTextButton(
                            text = "Forgot password?",
                            onClick = { viewModel.goTo(LoginStep.FORGOT) },
                            testTag = "login_forgot"
                        )
                        BansifyTextButton(
                            text = "Create account",
                            onClick = { viewModel.goTo(LoginStep.SIGN_UP) },
                            testTag = "login_create"
                        )
                    }
                }

                LoginStep.SIGN_UP -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                        TextFieldOutlined(
                            value = state.firstName,
                            onValueChange = viewModel::onFirstNameChange,
                            placeholder = "First name",
                            modifier = Modifier.weight(1f),
                            testTag = "signup_first_name"
                        )
                        TextFieldOutlined(
                            value = state.lastName,
                            onValueChange = viewModel::onLastNameChange,
                            placeholder = "Last name",
                            modifier = Modifier.weight(1f),
                            testTag = "signup_last_name"
                        )
                    }
                    Spacer(Modifier.height(Space.sm))
                    TextFieldOutlined(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        placeholder = "username",
                        label = "Username",
                        supportingText = if (state.username.isBlank()) null else "${state.username}@gothwadtech.com",
                        testTag = "signup_username"
                    )
                    Spacer(Modifier.height(Space.sm))
                    TextFieldOutlined(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "At least 8 characters",
                        label = "Password",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        testTag = "signup_password"
                    )
                    Spacer(Modifier.height(Space.sm))
                    TextFieldOutlined(
                        value = state.recoveryEmail,
                        onValueChange = viewModel::onRecoveryEmailChange,
                        placeholder = "you@example.com",
                        label = "Recovery email",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = state.error != null,
                        supportingText = state.error,
                        testTag = "signup_recovery_email"
                    )
                    Spacer(Modifier.height(Space.md))
                    PrimaryButton(
                        text = if (state.isBusy) "Creating…" else "Create account",
                        onClick = viewModel::signUp,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "signup_submit"
                    )
                    Spacer(Modifier.height(Space.xs))
                    BansifyTextButton(
                        text = "I already have an account",
                        onClick = { viewModel.goTo(LoginStep.IDENTIFIER) }
                    )
                }

                LoginStep.MFA -> {
                    TextFieldOutlined(
                        value = state.mfaCode,
                        onValueChange = viewModel::onMfaCodeChange,
                        placeholder = "123456",
                        label = "Verification code",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        isError = state.error != null,
                        supportingText = state.error,
                        testTag = "mfa_code"
                    )
                    Spacer(Modifier.height(Space.md))
                    PrimaryButton(
                        text = if (state.isBusy) "Checking…" else "Verify",
                        onClick = viewModel::verifyMfa,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "mfa_submit"
                    )
                    Spacer(Modifier.height(Space.xs))
                    BansifyTextButton(
                        text = "Back to sign in",
                        onClick = { viewModel.goTo(LoginStep.IDENTIFIER) }
                    )
                }

                LoginStep.FORGOT -> {
                    TextFieldOutlined(
                        value = state.identifier,
                        onValueChange = viewModel::onIdentifierChange,
                        placeholder = "username",
                        label = "Account",
                        testTag = "forgot_identifier"
                    )
                    Spacer(Modifier.height(Space.md))
                    PrimaryButton(
                        text = if (state.isBusy) "Sending…" else "Send reset link",
                        onClick = viewModel::sendResetLink,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "forgot_submit"
                    )
                    Spacer(Modifier.height(Space.xs))
                    BansifyTextButton(
                        text = "Back to sign in",
                        onClick = { viewModel.goTo(LoginStep.IDENTIFIER) }
                    )
                }
            }

            Spacer(Modifier.height(Space.md))
            TonalButton(
                text = "Continue as guest",
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth(),
                testTag = "login_guest"
            )
            Spacer(Modifier.height(Space.sm))
            Text(
                text = "Guest mode keeps chats on this device and does not use your Gothwad account.",
                style = BansifyTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Space.lg))
        }
    }
}
