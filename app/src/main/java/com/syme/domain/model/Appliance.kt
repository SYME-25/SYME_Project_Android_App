package com.syme.domain.model

import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.ApplianceType

data class Appliance(
    val applianceId: String = "",
    val circuitId: String = "",
    val name: String = "",
    val type: ApplianceType = ApplianceType.OTHER,
    val heatType: ApplianceHeatType = ApplianceHeatType.NON_THERMAL,
    val isSmart: Boolean = false,
    val powerWatt: Float = 0f,           // Puissance en watts
    val powerFactor: Float = 0f,        // Facteur de puissance
    val usageHoursPerDay: Float = 0f, // Durée d'utilisation par jour
    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability()
)
