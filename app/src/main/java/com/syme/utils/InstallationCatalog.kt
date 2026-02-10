package com.syme.utils

import com.syme.domain.model.Installation
import com.syme.domain.model.enumeration.InstallationType
import com.syme.R
import java.util.Locale

val installationCatalog = listOf(
    Installation(
        installationId = "residential",
        name = "Residential",
        type = InstallationType.RESIDENTIAL,
        energyWh = 1000.0,
        metadata = mapOf(
            "description" to R.string.installation_residential_desc
        )
    ),

    Installation(
        installationId = "commercial",
        name = "Commercial",
        type = InstallationType.COMMERCIAL,
        energyWh = 2000.0,
        metadata = mapOf(
            "description" to R.string.installation_commercial_desc
        )
    ),

    Installation(
        installationId = "industrial",
        name = "Industrial",
        type = InstallationType.INDUSTRIAL,
        energyWh = 3000.0,
        metadata = mapOf(
            "description" to R.string.installation_industrial_desc
        )
    ),

    Installation(
        installationId = "agricultural",
        name = "Agricultural",
        type = InstallationType.AGRICULTURAL,
        energyWh = 4000.0,
        metadata = mapOf(
            "description" to R.string.installation_agricultural_desc
        )
    ),

    Installation(
        installationId = "public",
        name = "Public Building",
        type = InstallationType.PUBLIC,
        energyWh = 5000.0,
        metadata = mapOf(
            "description" to R.string.installation_public_desc
        )
    ),

    Installation(
        installationId = "other",
        name = "Other",
        type = InstallationType.OTHER,
        energyWh = 6000.0,
        metadata = mapOf(
            "description" to R.string.installation_other_desc
        )
    )
).sortedBy { it.installationId.lowercase(Locale.getDefault()) }
