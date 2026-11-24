package com.bansify.ai

import android.app.Application
import android.util.Log
import com.bansify.accounts.core.BrokerPolicy
import com.bansify.accounts.core.BrokerTrustPolicy
import com.bansify.accounts.core.GothwadAccounts
import com.bansify.ai.auth.AiEndpoints

/**
 * Application entry point.
 *
 * The shared Bansify Accounts SDK must be initialised here, before anything reads
 * `GothwadAccounts.state`: its session is a lateinit, and touching it earlier
 * throws `UninitializedPropertyAccessException` on launch.
 */
class BansifyAiApplication : Application() {

    /** Set when the Accounts SDK could not start; the UI shows it instead of crashing. */
    var startupError: String? = null
        private set

    override fun onCreate() {
        super.onCreate()
        initSharedAccountsSdk()
    }

    /**
     * One Bansify Account signs you in on ai.bansify.com and in this app. The SDK
     * owns the PKCE flow, the encrypted token store, refresh and the account
     * centre, so this app never sees a password and never stores a raw token.
     */
    private fun initSharedAccountsSdk() {
        runCatching {
            GothwadAccounts.init(this, AiEndpoints.gothwadConfig(useCustomScheme = BuildConfig.DEBUG))
        }.onFailure { error ->
            startupError = error.stackTraceToString().lineSequence().take(4).joinToString("\n")
            Log.e("BansifyAiApplication", "Accounts SDK init failed", error)
        }
    }
}
