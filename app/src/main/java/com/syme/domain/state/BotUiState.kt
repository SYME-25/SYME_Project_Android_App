package com.syme.domain.state

import com.syme.domain.model.ChatMessage
import com.syme.domain.model.Conversation
import com.syme.domain.model.UserContext

data class BotUiState(
    val conversations: List<Conversation> = listOf(Conversation()),
    val currentConvId: String = conversations.first().id,  // ← plus jamais ""
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isRecording: Boolean = false,
    val showAttachOptions: Boolean = false,
    val showDrawer: Boolean = false,
    val userContext: UserContext = UserContext()
) {
    val currentConv: Conversation
        get() = conversations.find { it.id == currentConvId }
            ?: conversations.first()

    // ← Compose peut observer ce val directement comme clé de recomposition
    val messages: List<ChatMessage>
        get() = currentConv.messages
}