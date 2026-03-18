package com.syme.domain.model

data class Bill(

    val billId: String = "",
    val installationId: String = "",
    val ownerId: String = "",

    val periodStart: Long = 0L,
    val periodEnd: Long = 0L,
    val periodLabel: String = "",

    val energyWh: Double = 0.0,
    val hours: Double = 0.0,
    val powerSubscribed: Double = 0.0,
    val peakPowerW: Double = 0.0,

    val pricePerKWh: Double = 0.0,
    val amountToPay: Double = 0.0,
    val currency: String = "XAF",

    val isPaid: Boolean = false,
    val dueDate: Long = 0L,

    val metadata: BillingMetadata? = null,
    val trace: Traceability = Traceability()
)