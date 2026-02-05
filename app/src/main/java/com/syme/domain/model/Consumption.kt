package com.syme.domain.model

import com.syme.domain.model.enumeration.ConsumptionStateType

data class Consumption (
    val consumptionId: String = "",                  // Unique identifier
    val installationId: String? = null,                 // Linked installation
    val meterId: String? = null,                     // Linked meter
    val periodStart: Long = 0L,                      // Start of measurement period (epoch ms)
    val periodEnd: Long = 0L,                        // End of measurement period (epoch ms)
    val totalEnergy_kWh: Double = 0.0,               // Energy consumed in kWh
    val totalEnergy_kWhConsummed: Double = 0.0,      // Energy consumed in kWh
    val consumptionState: ConsumptionStateType = ConsumptionStateType.WAITING,
    val onDemand: Boolean = false,                   // True if consumption was manually triggered
    val metadata: Map<String, Any>? = null
)