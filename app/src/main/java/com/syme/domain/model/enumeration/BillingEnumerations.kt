package com.syme.domain.model.enumeration

enum class DemandStatus {
    /** Demand submitted, awaiting validation */
    PENDING,
    /** Demand accepted and active */
    ACTIVE,
    /** Demand completed (end time passed) */
    COMPLETED,
    /** Demand rejected or cancelled */
    CANCELLED
}

enum class InvoiceStatus {
    /** Invoice computed but not yet sent */
    DRAFT,
    /** Invoice issued to the client */
    ISSUED,
    /** Payment received */
    PAID,
    /** Payment overdue */
    OVERDUE,
    /** Invoice cancelled or voided */
    CANCELLED
}
