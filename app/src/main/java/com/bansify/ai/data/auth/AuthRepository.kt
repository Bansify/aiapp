package com.bansify.ai.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val accounts: List<AccountSession> = emptyList(),
    val activeUserId: String? = null,
    val isRestoring: Boolean = true
) {
    val activeAccount: AccountSession? get() = accounts.firstOrNull { it.userId == activeUserId } ?: accounts.firstOrNull()
    val isSignedIn: Boolean get() = activeAccount != null
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class MfaRequired(val challenge: String, val message: String) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * Single source of truth for Gothwad sign-in across the app. Chat and settings screens read
 * [state] and the account sheet mutates it through the methods below.
 */
class AuthRepository(
    private val client: AccountsClient,
    private val store: AccountStore
) {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun restore() {
        val (accounts, active) = store.read()
        _state.value = AuthState(accounts = accounts, activeUserId = active ?: accounts.firstOrNull()?.userId, isRestoring = false)
    }

    suspend fun signIn(identifier: String, password: String): AuthResult {
        if (identifier.isBlank() || password.isEmpty()) return AuthResult.Failure("Enter your username and password.")
        return when (val result = client.signIn(identifier, password)) {
            is AccountsClient.SignInResult.Success -> {
                remember(result.session)
                AuthResult.Success
            }
            is AccountsClient.SignInResult.MfaRequired -> AuthResult.MfaRequired(result.challenge, result.message)
            is AccountsClient.SignInResult.Failure -> AuthResult.Failure(result.message)
        }
    }

    suspend fun verifyMfa(challenge: String, code: String): AuthResult =
        when (val result = client.verifyMfa(challenge, code)) {
            is AccountsClient.SignInResult.Success -> {
                remember(result.session)
                AuthResult.Success
            }
            is AccountsClient.SignInResult.MfaRequired -> AuthResult.MfaRequired(result.challenge, result.message)
            is AccountsClient.SignInResult.Failure -> AuthResult.Failure(result.message)
        }

    suspend fun signUp(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        recoveryEmail: String
    ): AuthResult {
        if (username.length < 3) return AuthResult.Failure("Username must be at least 3 characters.")
        if (password.length < 8) return AuthResult.Failure("Password must be at least 8 characters.")
        if (!recoveryEmail.contains('@')) return AuthResult.Failure("Enter a valid recovery email.")
        return when (val result = client.signUp(firstName, lastName, username, password, recoveryEmail)) {
            is AccountsClient.SignInResult.Success -> {
                remember(result.session)
                AuthResult.Success
            }
            is AccountsClient.SignInResult.MfaRequired -> AuthResult.MfaRequired(result.challenge, result.message)
            is AccountsClient.SignInResult.Failure -> AuthResult.Failure(result.message)
        }
    }

    suspend fun checkUsername(username: String): AccountsClient.UsernameCheck = client.checkUsername(username)

    suspend fun forgotPassword(username: String): String = client.forgotPassword(username)

    suspend fun switchTo(userId: String) {
        val accounts = _state.value.accounts
        if (accounts.none { it.userId == userId }) return
        _state.value = _state.value.copy(activeUserId = userId)
        store.write(accounts, userId)
    }

    suspend fun signOut(userId: String) {
        val remaining = _state.value.accounts.filterNot { it.userId == userId }
        val nextActive = _state.value.activeUserId.takeIf { id -> remaining.any { it.userId == id } }
            ?: remaining.firstOrNull()?.userId
        _state.value = _state.value.copy(accounts = remaining, activeUserId = nextActive)
        store.write(remaining, nextActive)
    }

    suspend fun signOutAll() {
        _state.value = _state.value.copy(accounts = emptyList(), activeUserId = null)
        store.write(emptyList(), null)
    }

    /** Returns a usable access token, refreshing it when it is close to expiry. */
    suspend fun freshAccessToken(userId: String): String? {
        val account = _state.value.accounts.firstOrNull { it.userId == userId } ?: return null
        if (account.accessToken.isNotBlank() && account.expiresAtMillis > System.currentTimeMillis() + 60_000) {
            return account.accessToken
        }
        val refresh = account.refreshToken ?: return account.accessToken.takeIf { it.isNotBlank() }
        val renewed = client.refresh(refresh) ?: return account.accessToken.takeIf { it.isNotBlank() }
        val updated = account.copy(
            accessToken = renewed.accessToken,
            refreshToken = renewed.refreshToken ?: account.refreshToken,
            expiresAtMillis = System.currentTimeMillis() + renewed.expiresInSeconds * 1000
        )
        val accounts = _state.value.accounts.map { if (it.userId == userId) updated else it }
        _state.value = _state.value.copy(accounts = accounts)
        store.write(accounts, _state.value.activeUserId)
        return updated.accessToken
    }

    private suspend fun remember(session: AccountsClient.SignedIn) {
        val account = AccountSession(
            userId = session.userId.ifBlank { session.username.ifBlank { session.email } },
            username = session.username,
            email = session.email,
            firstName = session.firstName,
            lastName = session.lastName,
            avatarUrl = session.avatarUrl,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAtMillis = System.currentTimeMillis() + session.expiresInSeconds * 1000
        )
        val others = _state.value.accounts.filterNot { it.userId == account.userId }
        val accounts = (listOf(account) + others).take(6)
        _state.value = _state.value.copy(accounts = accounts, activeUserId = account.userId, isRestoring = false)
        store.write(accounts, account.userId)
    }
}
