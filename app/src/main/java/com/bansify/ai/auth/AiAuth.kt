package com.bansify.ai.auth

import android.app.Activity
import com.bansify.accounts.core.AccountState
import com.bansify.accounts.core.AuthFailure
import com.bansify.accounts.core.GothwadAccount
import com.bansify.accounts.core.GothwadAccounts
import kotlinx.coroutines.flow.StateFlow

/**
 * Bansify AI's thin facade over the shared Bansify Accounts SDK.
 *
 * The app ships no PKCE client, token store or account centre of its own: the SDK
 * owns credentials, refresh and cross-app discovery, so sign-in looks identical
 * here and in Mail, Drive and Store. This class only exposes what the UI needs.
 */
class AiAuth : AiAccessTokenProvider {

    /** Sign-in state for the whole app (Initializing / SignedOut / SignedIn / StorageCorrupted). */
    val state: StateFlow<AccountState> get() = GothwadAccounts.state

    /** Last sign-in failure, cleared by the UI once it has been shown. */
    val failure: StateFlow<AuthFailure?> get() = GothwadAccounts.error

    override suspend fun accessToken(): String? = GothwadAccounts.currentAccessToken()

    /** Browser (Custom Tabs) OAuth 2.0 + PKCE sign-in. */
    fun signIn(activity: Activity, chooseAccount: Boolean = false) {
        GothwadAccounts.signInWithBrowser(activity, chooseAccount)
    }

    fun signOut(userId: String) {
        GothwadAccounts.signOut(userId)
    }

    fun switchTo(userId: String) {
        GothwadAccounts.switchTo(userId)
    }

    fun clearFailure() {
        GothwadAccounts.clearError()
    }

    fun currentAccount(state: AccountState): GothwadAccount? =
        (state as? AccountState.SignedIn)?.account
}

/** Supplies the bearer token for the active account, refreshing it when needed. */
fun interface AiAccessTokenProvider {
    /** Returns a valid access token, or `null` when nobody is signed in. */
    suspend fun accessToken(): String?
}
