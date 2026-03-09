package com.syme.utils

import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.R
import java.util.Locale

val installationCatalog = listOf(
    Installation(
        installationId = "1",
        name = "Residential",
        type = InstallationType.RESIDENTIAL,
        energyWh = 1000.0,
        metadata = mapOf(
            "description" to R.string.installation_residential_desc
        )
    ),

    Installation(
        installationId = "2",
        name = "Commercial",
        type = InstallationType.COMMERCIAL,
        energyWh = 2000.0,
        metadata = mapOf(
            "description" to R.string.installation_commercial_desc
        )
    ),

    Installation(
        installationId = "3",
        name = "Industrial",
        type = InstallationType.INDUSTRIAL,
        energyWh = 3000.0,
        metadata = mapOf(
            "description" to R.string.installation_industrial_desc
        )
    ),

    Installation(
        installationId = "4",
        name = "Agricultural",
        type = InstallationType.AGRICULTURAL,
        energyWh = 4000.0,
        metadata = mapOf(
            "description" to R.string.installation_agricultural_desc
        )
    ),

    Installation(
        installationId = "5",
        name = "Public Building",
        type = InstallationType.PUBLIC,
        energyWh = 5000.0,
        metadata = mapOf(
            "description" to R.string.installation_public_desc
        )
    ),

    Installation(
        installationId = "6",
        name = "Other",
        type = InstallationType.OTHER,
        energyWh = 6000.0,
        metadata = mapOf(
            "description" to R.string.installation_other_desc
        )
    )
).sortedBy { it.installationId.toInt() }
