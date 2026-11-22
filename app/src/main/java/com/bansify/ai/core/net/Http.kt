package com.bansify.ai.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** Result of a non-streaming HTTP call. */
data class HttpResult(val code: Int, val body: String) {
    val isSuccess: Boolean get() = code in 200..299
}

/**
 * Minimal HTTP layer built on HttpURLConnection so the app needs no extra networking
 * dependency. All calls run on Dispatchers.IO.
 */
object Http {

    suspend fun postJson(
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String,
        readTimeoutMs: Int = 60_000
    ): HttpResult = withContext(Dispatchers.IO) {
        val connection = open(url, headers, readTimeoutMs)
        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            HttpResult(code, text)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getJson(
        url: String,
        headers: Map<String, String> = emptyMap(),
        readTimeoutMs: Int = 30_000
    ): HttpResult = withContext(Dispatchers.IO) {
        val connection = open(url, headers, readTimeoutMs, method = "GET")
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            HttpResult(code, text)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Server-sent-events POST. [onData] receives every `data:` payload without the prefix.
     * [shouldStop] lets the caller abort a running stream (used when the user taps Stop).
     */
    suspend fun postSse(
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String,
        readTimeoutMs: Int = 120_000,
        shouldStop: () -> Boolean = { false },
        onData: (String) -> Unit
    ): HttpResult = withContext(Dispatchers.IO) {
        val connection = open(url, headers, readTimeoutMs)
        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val text = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                return@withContext HttpResult(code, text)
            }
            BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
                while (!shouldStop()) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload.isEmpty()) continue
                    if (payload == "[DONE]") break
                    onData(payload)
                }
            }
            HttpResult(200, "")
        } finally {
            connection.disconnect()
        }
    }

    private fun open(
        url: String,
        headers: Map<String, String>,
        readTimeoutMs: Int,
        method: String = "POST"
    ): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = method
        connectTimeout = 20_000
        readTimeout = readTimeoutMs
        doOutput = method == "POST"
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Accept", "text/event-stream, application/json")
        headers.forEach { (key, value) -> setRequestProperty(key, value) }
    }
}
