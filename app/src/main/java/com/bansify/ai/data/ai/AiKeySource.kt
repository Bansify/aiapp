package com.bansify.ai.data.ai

import kotlinx.coroutines.flow.StateFlow

/** Small interface so the chat layer can read saved keys without depending on the repository. */
interface AiKeySource {
    val keys: StateFlow<Map<String, String>>
}
