package com.syme.ui.component.compositionlocal

import androidx.compose.runtime.staticCompositionLocalOf
import com.syme.data.session.SessionManager
import com.syme.domain.model.User

val LocalCurrentUserSession = staticCompositionLocalOf<User?> { null }
