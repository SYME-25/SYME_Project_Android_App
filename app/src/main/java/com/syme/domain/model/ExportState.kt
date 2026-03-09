package com.syme.domain.model

import android.content.Intent
import java.io.File


sealed class ExportState {
    object Idle    : ExportState()
    object Loading : ExportState()
    data class Success(val intent: Intent, val file: File) : ExportState()
    data class Error(val message: String)   : ExportState()
}
