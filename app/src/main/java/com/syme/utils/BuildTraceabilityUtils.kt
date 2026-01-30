package com.syme.utils

import com.syme.domain.model.Traceability

/**
 * Generates a Traceability instance depending on creation or update.
 */
fun buildTraceability(
    existing: Traceability?,     // null = creation, otherwise = editing
    currentUserId: String,       // the admin/technician/user performing the action
    currentUserRole: String = "USER"
): Traceability {

    val now = System.currentTimeMillis()

    return existing?.// Editing
    copy(
        updatedAt = now,
        updatedById = currentUserId,
        updatedByRole = currentUserRole,
        version = existing.version + 1
    )
        ?: // Creation
        Traceability(
            createdAt = now,
            updatedAt = now,
            createdById = currentUserId,
            updatedById = currentUserId,
            createdByRole = currentUserRole,
            updatedByRole = currentUserRole,
            version = 1,
            active = true
        )
}
