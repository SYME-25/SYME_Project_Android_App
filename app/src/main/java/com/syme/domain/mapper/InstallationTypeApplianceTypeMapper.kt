package com.syme.domain.mapper

import com.syme.domain.model.enumeration.ApplianceType
import com.syme.domain.model.enumeration.InstallationType

val InstallationType.allowedApplianceTypes: List<ApplianceType>
    get() = when (this) {

        InstallationType.RESIDENTIAL -> listOf(
            ApplianceType.REFRIGERATOR,
            ApplianceType.FREEZER,
            ApplianceType.WASHING_MACHINE,
            ApplianceType.DISHWASHER,
            ApplianceType.MICROWAVE,
            ApplianceType.OVEN,
            ApplianceType.TV,
            ApplianceType.LIGHTING,
            ApplianceType.AIR_CONDITIONER,
            ApplianceType.ELECTRICAL_VEHICLE
        )

        InstallationType.COMMERCIAL -> listOf(
            ApplianceType.TV,
            ApplianceType.LIGHTING,
            ApplianceType.PRINTER,
            ApplianceType.DESKTOP_COMPUTER,
            ApplianceType.LAPTOP_COMPUTER,
            ApplianceType.AIR_CONDITIONER,
            ApplianceType.SOFT_DRINK_MACHINE,
            ApplianceType.ELECTRICAL_VEHICLE,
            ApplianceType.VENDING_MACHINE
        )

        InstallationType.INDUSTRIAL -> listOf(
            ApplianceType.ELECTRIC_MOTOR,
            ApplianceType.PUMP,
            ApplianceType.CONVEYOR_BELT,
            ApplianceType.ROBOTIC_ARM,
            ApplianceType.FACTORY_MACHINE,
            ApplianceType.COOLING_UNIT,
            ApplianceType.HVAC,
            ApplianceType.ELECTRICAL_VEHICLE,
            ApplianceType.GENERATOR
        )

        InstallationType.AGRICULTURAL -> listOf(
            ApplianceType.PUMP,
            ApplianceType.ELECTRIC_MOTOR,
            ApplianceType.FAN,
            ApplianceType.ELECTRICAL_VEHICLE,
            ApplianceType.FARM
        )

        InstallationType.PUBLIC -> listOf(
            ApplianceType.LIGHTING,
            ApplianceType.TV,
            ApplianceType.AIR_CONDITIONER,
            ApplianceType.HOSPITAL,
            ApplianceType.ELECTRICAL_VEHICLE,
            ApplianceType.OFFICE_BUILDING
        )

        InstallationType.OTHER -> ApplianceType.entries
    }
