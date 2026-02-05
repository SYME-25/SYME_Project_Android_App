package com.syme.domain.mapper

import com.syme.domain.model.enumeration.InstallationType
import com.syme.R

val InstallationType.labelResId: Int
    get() = when (this) {
        InstallationType.OTHER -> R.string.home_installation_filter_other
        InstallationType.RESIDENTIAL -> R.string.home_installation_filter_residential
        InstallationType.COMMERCIAL -> R.string.home_installation_filter_commercial
        InstallationType.INDUSTRIAL -> R.string.home_installation_filter_industrial
        InstallationType.AGRICULTURAL -> R.string.home_installation_filter_agricultural
        InstallationType.PUBLIC -> R.string.home_installation_filter_public
    }

val InstallationType.imageResId: Int
    get() = when (this) {
        InstallationType.OTHER -> R.drawable.building_town_svgrepo_com
        InstallationType.RESIDENTIAL -> R.drawable.maison
        InstallationType.COMMERCIAL -> R.drawable.boutique
        InstallationType.INDUSTRIAL -> R.drawable.factory_svgrepo_com
        InstallationType.AGRICULTURAL -> R.drawable.barn_svgrepo_com
        InstallationType.PUBLIC -> R.drawable.immeuble_de_bureaux
    }