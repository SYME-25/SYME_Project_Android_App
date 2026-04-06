package com.syme.ui.component.compositionlocal

import androidx.compose.runtime.staticCompositionLocalOf
import com.syme.domain.model.User

val LocalCurrentUserSession = staticCompositionLocalOf<User?> { null }
