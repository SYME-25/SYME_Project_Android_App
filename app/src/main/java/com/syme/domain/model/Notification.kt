package com.syme.domain.model

import com.syme.domain.model.enumeration.NotificationCategory
import com.syme.domain.model.enumeration.NotificationType

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val installationId: String = "",

    val title: String = "",
    val body: String = "",

    val type: NotificationType = NotificationType.INFO,
    val category: NotificationCategory = NotificationCategory.SYSTEM,

    val isRead: Boolean = false,

    val targetScreen: String? = null,
    val targetId: String? = null,

    val trace: Traceability = Traceability(),
    val metadata: Map<String, String> = emptyMap()
)