package com.syme.domain.model

import android.net.Uri
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentUri: Uri? = null,
    val attachmentName: String? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val audioUri: Uri? = null,          // ← URI du fichier audio enregistré
    val audioDurationSec: Int = 0       // ← durée en secondes
)
