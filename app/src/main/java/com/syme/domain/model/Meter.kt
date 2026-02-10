package com.syme.domain.model

import com.syme.domain.model.enumeration.MeterStatus
import com.syme.domain.model.enumeration.MeterType

data class Meter(
    val meterId: String = "",
    val installationId: String = "",
    val meterType: MeterType = MeterType.SLAVE,           //Slave or Master
    val status: MeterStatus = MeterStatus.ACTIVE,
    val relays: List<Relay> = emptyList(),

    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
)
