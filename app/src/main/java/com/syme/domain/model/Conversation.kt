package com.syme.domain.model

import java.util.UUID

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "new_conversation",   // resolved via stringResource at call site
    val messages: List<ChatMessage> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
