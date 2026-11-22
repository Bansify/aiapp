package com.bansify.ai.data.auth

import com.bansify.ai.core.net.Http
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Client for the live Gothwad Accounts Worker (`accounts.<domain>/api`).
 *
 * These are the cookie-free JSON endpoints the Accounts and Mail Android apps already use,
 * so signing in here is the same account, the same sessions and the same sign-out.
 */
class AccountsClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val clientId: String = DEFAULT_CLIENT_ID,
    private val scope: String = DEFAULT_SCOPE,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) {

    data class SignedIn(
        val accessToken: String,
        val refreshToken: String?,
        val expiresInSeconds: Long,
        val sessionId: String?,
        val userId: String,
        val username: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val avatarUrl: String?
    )

    sealed interface SignInResult {
        data class Success(val session: SignedIn) : SignInResult
        data class MfaRequired(val challenge: String, val message: String) : SignInResult
        data class Failure(val message: String) : SignInResult
    }

    data class UsernameCheck(val available: Boolean, val valid: Boolean, val error: String?)

    suspend fun checkIdentifier(identifier: String): Pair<Boolean, String?> {
        val result = Http.postJson(
            "$baseUrl/mobile/auth/check-identifier",
            body = buildJsonObject { put("identifier", identifier.trim()) }.toString()
        )
        val root = parse(result.body)
        if (!result.isSuccess) return false to (message(root) ?: "Could not reach Gothwad Accounts.")
        val exists = root?.get("exists")?.jsonPrimitive?.content?.toBoolean() ?: false
        return exists to message(root)
    }

    suspend fun checkUsername(username: String): UsernameCheck {
        val result = Http.postJson(
            "$baseUrl/mobile/auth/check-username",
            body = buildJsonObject { put("username", username.trim().lowercase()) }.toString()
        )
        val root = parse(result.body)
        if (!result.isSuccess) {
            return UsernameCheck(available = false, valid = false, error = message(root) ?: "Could not check that username.")
        }
        return UsernameCheck(
            available = root?.get("available")?.jsonPrimitive?.content?.toBoolean() ?: false,
            valid = root?.get("valid")?.jsonPrimitive?.content?.toBoolean() ?: false,
            error = message(root)
        )
    }

    suspend fun signIn(identifier: String, password: String): SignInResult =
        authCall("$baseUrl/mobile/auth/signin") {
            put("client_id", clientId)
            put("scope", scope)
            put("identifier", identifier.trim())
            put("password", password)
        }

    suspend fun verifyMfa(challenge: String, code: String): SignInResult =
        authCall("$baseUrl/mobile/auth/signin/mfa") {
            put("client_id", clientId)
            put("scope", scope)
            put("challenge", challenge)
            put("code", code.trim())
        }

    suspend fun signUp(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        recoveryEmail: String
    ): SignInResult = authCall("$baseUrl/mobile/auth/signup") {
        put("client_id", clientId)
        put("scope", scope)
        put("first_name", firstName.trim())
        put("last_name", lastName.trim())
        put("username", username.trim().lowercase())
        put("password", password)
        put("recovery_email", recoveryEmail.trim())
    }

    suspend fun forgotPassword(username: String): String {
        val result = Http.postJson(
            "$baseUrl/mobile/auth/forgot-password",
            body = buildJsonObject { put("username", username.trim()) }.toString()
        )
        val root = parse(result.body)
        return message(root) ?: if (result.isSuccess) "Check your recovery email for a reset link." else "Could not send the reset email."
    }

    suspend fun userInfo(accessToken: String): JsonObject? {
        val result = Http.getJson(
            "$baseUrl/oauth/userinfo",
            headers = mapOf("Authorization" to "Bearer $accessToken")
        )
        if (!result.isSuccess) return null
        return parse(result.body)
    }

    suspend fun refresh(refreshToken: String): SignedIn? {
        val result = Http.postJson(
            "$baseUrl/oauth/token",
            body = buildJsonObject {
                put("grant_type", "refresh_token")
                put("refresh_token", refreshToken)
                put("client_id", clientId)
            }.toString()
        )
        if (!result.isSuccess) return null
        val root = parse(result.body) ?: return null
        val access = root["access_token"]?.jsonPrimitive?.content ?: return null
        return SignedIn(
            accessToken = access,
            refreshToken = root["refresh_token"]?.jsonPrimitive?.content ?: refreshToken,
            expiresInSeconds = root["expires_in"]?.jsonPrimitive?.content?.toLongOrNull() ?: 900L,
            sessionId = root["session_id"]?.jsonPrimitive?.content,
            userId = "",
            username = "",
            email = "",
            firstName = "",
            lastName = "",
            avatarUrl = null
        )
    }

    private suspend fun authCall(url: String, body: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit): SignInResult {
        val payload = buildJsonObject(body).toString()
        val result = Http.postJson(url, body = payload)
        val root = parse(result.body)
        if (root == null) {
            return SignInResult.Failure("Gothwad Accounts did not return a readable response (HTTP ${result.code}).")
        }
        val mfaRequired = root["mfa_required"]?.jsonPrimitive?.content?.toBoolean() ?: false
        if (mfaRequired) {
            val challenge = root["challenge"]?.jsonPrimitive?.content.orEmpty()
            return SignInResult.MfaRequired(
                challenge = challenge,
                message = root["message"]?.jsonPrimitive?.content ?: "Enter your authenticator code."
            )
        }
        val access = root["access_token"]?.jsonPrimitive?.content
        if (result.isSuccess && !access.isNullOrBlank()) {
            val user = root["user"] as? JsonObject
            return SignInResult.Success(
                SignedIn(
                    accessToken = access,
                    refreshToken = root["refresh_token"]?.jsonPrimitive?.content,
                    expiresInSeconds = root["expires_in"]?.jsonPrimitive?.content?.toLongOrNull() ?: 900L,
                    sessionId = root["session_id"]?.jsonPrimitive?.content,
                    userId = user?.get("id")?.jsonPrimitive?.content.orEmpty(),
                    username = user?.get("username")?.jsonPrimitive?.content.orEmpty(),
                    email = user?.get("email")?.jsonPrimitive?.content.orEmpty(),
                    firstName = user?.get("first_name")?.jsonPrimitive?.content.orEmpty(),
                    lastName = user?.get("last_name")?.jsonPrimitive?.content.orEmpty(),
                    avatarUrl = user?.get("avatar_url")?.jsonPrimitive?.content
                )
            )
        }
        return SignInResult.Failure(
            message(root) ?: "Sign-in failed (HTTP ${result.code})."
        )
    }

    private fun message(root: JsonObject?): String? {
        root ?: return null
        val candidates = listOf("message", "error_description", "error")
        for (key in candidates) {
            val value = root[key]?.jsonPrimitive?.content
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun parse(body: String): JsonObject? =
        runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull()

    companion object {
        const val DEFAULT_BASE_URL = "https://accounts.gothwadtech.com/api"

        /**
         * Accounts allows first-party native clients only. `gothwad-accounts-android` is the
         * client already approved in the live Worker, so no database change is needed to sign in.
         * Register a dedicated `gothwad-ai` client when you want app-specific scopes.
         */
        const val DEFAULT_CLIENT_ID = "gothwad-accounts-android"
        const val DEFAULT_SCOPE = "profile email offline_access"
        const val ACCOUNTS_WEB_URL = "https://accounts.gothwadtech.com"
    }
}
