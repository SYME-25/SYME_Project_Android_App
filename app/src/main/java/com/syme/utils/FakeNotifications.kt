package com.syme.utils

import com.syme.domain.model.Notification
import com.syme.domain.model.Traceability
import com.syme.domain.model.enumeration.NotificationCategory
import com.syme.domain.model.enumeration.NotificationType

fun fakeNotifications(): List<Notification> {
    val now = System.currentTimeMillis()

    return listOf(
        Notification(
            notificationId = "1",
            title = "Critical alert",
            body = "A major issue has been detected in your installation.",
            type = NotificationType.CRITICAL,
            category = NotificationCategory.SYSTEM,
            isRead = false,
            trace = Traceability(createdAt = now - 2 * 60_000) // 2 min ago
        ),
        Notification(
            notificationId = "2",
            title = "High consumption",
            body = "Your energy usage is higher than usual.",
            type = NotificationType.WARNING,
            category = NotificationCategory.CONSUMPTION,
            isRead = false,
            trace = Traceability(createdAt = now - 30 * 60_000)
        ),
        Notification(
            notificationId = "3",
            title = "Payment successful",
            body = "Your electricity bill has been paid.",
            type = NotificationType.SUCCESS,
            category = NotificationCategory.BILL,
            isRead = true,
            trace = Traceability(createdAt = now - 2 * 3_600_000)
        ),
        Notification(
            notificationId = "4",
            title = "New device connected",
            body = "A new meter has been added to your system.",
            type = NotificationType.INFO,
            category = NotificationCategory.METER,
            isRead = false,
            trace = Traceability(createdAt = now - 26 * 3_600_000) // yesterday
        ),
        Notification(
            notificationId = "5",
            title = "Relay activated",
            body = "A relay has been triggered remotely.",
            type = NotificationType.INFO,
            category = NotificationCategory.RELAY,
            isRead = true,
            trace = Traceability(createdAt = now - 3 * 86_400_000) // 3 days ago
        ),
        Notification(
            notificationId = "6",
            title = "New request",
            body = "A user submitted a new request.",
            type = NotificationType.WARNING,
            category = NotificationCategory.DEMAND,
            isRead = false,
            trace = Traceability(createdAt = now - 5 * 86_400_000)
        )
    )
}