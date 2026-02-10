package com.syme.domain.model

import com.syme.domain.model.enumeration.InstallationType

data class Installation(
    val installationId: String = "",
    val ownerId: String = "",

    val name: String = "",
    val type: InstallationType = InstallationType.RESIDENTIAL,

    // Localisation
    val location: Location = Location(),
    val address: String = "",

    //Energy by day
    val energyWh: Double = 0.0,

    // Production locale
    val hasSolarPanels: Boolean = false,
    val solarPowerKw: Double = 0.0,

    //Components
    val meter: List<Meter> = emptyList(),
    val circuits: List<Circuit> = emptyList(),

    val metadata: Map<String, Any>? = null,
    val trace: Traceability = Traceability(),
)

