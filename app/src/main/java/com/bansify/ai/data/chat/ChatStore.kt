package com.bansify.ai.data.chat

import com.bansify.ai.data.mock.ChatAttachment
import com.bansify.ai.data.mock.ChatMessage
import com.bansify.ai.data.mock.ChatMode
import com.bansify.ai.data.mock.Conversation
import com.bansify.ai.data.mock.MessageFeedback
import com.bansify.ai.data.mock.MessageRole
import com.bansify.ai.data.mock.TimeBucket
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File

/** Reads and writes the chat history as one JSON file on the device. */
class ChatStore(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun load(): List<Conversation> {
        if (!file.exists()) return emptyList()
        val raw = runCatching { file.readText() }.getOrNull().orEmpty()
        if (raw.isBlank()) return emptyList()
        val root = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: return emptyList()
        val array = root["conversations"] as? JsonArray ?: return emptyList()
        return array.mapNotNull { element -> runCatching { decodeConversation(element.jsonObject) }.getOrNull() }
    }

    fun save(conversations: List<Conversation>) {
        runCatching {
            file.parentFile?.mkdirs()
            val payload = buildJsonObject {
                put("version", 1)
                put("conversations", buildJsonArray { conversations.forEach { add(encodeConversation(it)) } })
            }.toString()
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.writeText(payload)
            if (file.exists()) file.delete()
            temp.renameTo(file)
        }
    }

    private fun encodeConversation(conversation: Conversation): JsonObject = buildJsonObject {
        put("id", conversation.id)
        put("title", conversation.title)
        put("preview", conversation.preview)
        put("relativeTime", conversation.relativeTime)
        put("timeBucket", conversation.timeBucket.name)
        put("modelId", conversation.modelId)
        put("modelLabel", conversation.modelLabel)
        put("isPinned", conversation.isPinned)
        put("isShared", conversation.isShared)
        put("mode", conversation.mode.name)
        put("messages", buildJsonArray { conversation.messages.forEach { add(encodeMessage(it)) } })
    }

    private fun encodeMessage(message: ChatMessage): JsonObject = buildJsonObject {
        put("id", message.id)
        put("role", message.role.name)
        put("content", message.content)
        put("timestampLabel", message.timestampLabel)
        put("dateGroup", message.dateGroup)
        message.modelName?.let { put("modelName", it) }
        put("feedback", message.feedback.name)
        if (message.attachments.isNotEmpty()) {
            put("attachments", buildJsonArray {
                message.attachments.forEach { attachment ->
                    add(
                        buildJsonObject {
                            put("id", attachment.id)
                            put("label", attachment.label)
                            put("type", attachment.type)
                        }
                    )
                }
            })
        }
    }

    private fun decodeConversation(root: JsonObject): Conversation = Conversation(
        id = root.string("id"),
        title = root.string("title"),
        preview = root.string("preview"),
        relativeTime = root.string("relativeTime").ifBlank { "Just now" },
        timeBucket = enumOr(root.string("timeBucket"), TimeBucket.TODAY),
        modelId = root.string("modelId"),
        modelLabel = root.string("modelLabel"),
        isPinned = root.boolean("isPinned"),
        isShared = root.boolean("isShared"),
        mode = enumOr(root.string("mode"), ChatMode.CHAT),
        messages = (root["messages"] as? JsonArray)?.mapNotNull {
            runCatching { decodeMessage(it.jsonObject) }.getOrNull()
        }.orEmpty()
    )

    private fun decodeMessage(root: JsonObject): ChatMessage = ChatMessage(
        id = root.string("id"),
        role = enumOr(root.string("role"), MessageRole.USER),
        content = root.string("content"),
        timestampLabel = root.string("timestampLabel").ifBlank { "Just now" },
        dateGroup = root.string("dateGroup").ifBlank { "Today" },
        modelName = root["modelName"]?.jsonPrimitive?.content,
        feedback = enumOr(root.string("feedback"), MessageFeedback.NONE),
        attachments = (root["attachments"] as? JsonArray)?.mapNotNull { element ->
            runCatching {
                val item = element.jsonObject
                ChatAttachment(
                    id = item.string("id"),
                    label = item.string("label"),
                    type = item.string("type")
                )
            }.getOrNull()
        }.orEmpty()
    )

    private fun JsonObject.string(key: String): String =
        this[key]?.jsonPrimitive?.content.orEmpty()

    private fun JsonObject.boolean(key: String): Boolean =
        this[key]?.jsonPrimitive?.content?.toBoolean() ?: false

    private inline fun <reified T : Enum<T>> enumOr(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
