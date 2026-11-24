package com.bansify.ai.auth

import com.bansify.accounts.core.BrokerPolicy
import com.bansify.accounts.core.BrokerTrustPolicy
import com.bansify.accounts.core.GothwadConfig
import com.bansify.accounts.core.Scope

/**
 * Authoritative endpoints for the `gothwad-ai` OAuth client.
 *
 * Values mirror the registration in `Bansify/accounts` (migration 0015) and the
 * AI Worker contract in `Bansify/ai`. Nothing here is user configurable at
 * runtime: a wrong host would either leak a bearer token or break App Link
 * verification.
 */
object AiEndpoints {

    /** Public first-party OAuth client id registered for the AI app and website. */
    const val CLIENT_ID: String = "gothwad-ai"

    /** Android package and application id of this client. */
    const val PACKAGE_NAME: String = "com.bansify.ai"

    /** Custom-scheme fallback redirect (debug builds and browsers without App Links). */
    const val REDIRECT_URI_CUSTOM_SCHEME: String = "com.bansify.ai:/oauth/callback"

    /** Verified HTTPS App Link redirect used in production. */
    const val REDIRECT_URI_APP_LINK: String = "https://ai.bansify.com/oauth/callback"

    /** Accounts API base (the Worker custom domain; the Pages host answers HTML). */
    const val ACCOUNTS_API_BASE_URL: String = "https://accounts.bansify.com/api"

    /** Accounts web base, for "manage account" links. */
    const val ACCOUNTS_WEB_URL: String = "https://accounts.bansify.com"

    /** Bansify AI Worker API base — the only host that ever receives the AI bearer token. */
    const val AI_API_BASE_URL: String = "https://ai.bansify.com/api"

    /** Host that may receive the AI bearer token. Everything else must stay token free. */
    const val AI_API_HOST: String = "ai.bansify.com"

    /**
     * Scopes for this client. Chat history is the app's own data, so it needs no
     * account-wide scope beyond identity.
     */
    val SCOPES: Set<Scope> = setOf(Scope.Profile, Scope.Email, Scope.OfflineAccess)

    /**
     * @param useCustomScheme debug builds fall back to `com.bansify.ai:/oauth/callback`
     *   because a debug install cannot own the verified App Link.
     */
    fun gothwadConfig(useCustomScheme: Boolean = false): GothwadConfig = GothwadConfig(
        clientId = CLIENT_ID,
        redirectUri = if (useCustomScheme) REDIRECT_URI_CUSTOM_SCHEME else REDIRECT_URI_APP_LINK,
        scopes = SCOPES,
        authBaseUrl = ACCOUNTS_API_BASE_URL,
        brokerPolicy = BrokerPolicy.PeerDiscovery(
            trustPolicy = BrokerTrustPolicy.SameSigningCertificate(allowDebugBuild = useCustomScheme),
        ),
        allowCustomSchemeRedirect = useCustomScheme,
    )
}
