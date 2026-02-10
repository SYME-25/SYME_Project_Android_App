package com.syme.domain.mapper

import com.syme.R
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.ApplianceType

val ApplianceType.labelResId: Int
    get() = when (this) {

        // 🏠 Domestique
        ApplianceType.REFRIGERATOR -> R.string.home_appliance_filter_refrigerator
        ApplianceType.FREEZER -> R.string.home_appliance_filter_freezer
        ApplianceType.WASHING_MACHINE -> R.string.home_appliance_filter_washing_machine
        ApplianceType.DISHWASHER -> R.string.home_appliance_filter_dishwasher
        ApplianceType.MICROWAVE -> R.string.home_appliance_filter_microwave
        ApplianceType.OVEN -> R.string.home_appliance_filter_oven
        ApplianceType.COOKING_STOVE -> R.string.home_appliance_filter_cooking_stove
        ApplianceType.TOASTER -> R.string.home_appliance_filter_toaster
        ApplianceType.MIXER -> R.string.home_appliance_filter_mixer
        ApplianceType.COFFEE_MAKER -> R.string.home_appliance_filter_coffee_maker
        ApplianceType.IRON -> R.string.home_appliance_filter_iron
        ApplianceType.VACUUM_CLEANER -> R.string.home_appliance_filter_vacuum
        ApplianceType.HAIR_DRYER -> R.string.home_appliance_filter_hair_dryer
        ApplianceType.SHAVER -> R.string.home_appliance_filter_shaver
        ApplianceType.FAN -> R.string.home_appliance_filter_fan
        ApplianceType.TOWER_FAN -> R.string.home_appliance_filter_tower_fan
        ApplianceType.AIR_CONDITIONER -> R.string.home_appliance_filter_air_conditioner
        ApplianceType.RADIATOR -> R.string.home_appliance_filter_radiator
        ApplianceType.ELECTRICAL_VEHICLE -> R.string.home_appliance_filter_electric_vehicle

        // 💡
        ApplianceType.LIGHTING -> R.string.home_appliance_filter_lighting

        // 💻 Multimédia
        ApplianceType.TV -> R.string.home_appliance_filter_tv
        ApplianceType.DESKTOP_COMPUTER -> R.string.home_appliance_filter_desktop_computer
        ApplianceType.LAPTOP_COMPUTER -> R.string.home_appliance_filter_laptop_computer
        ApplianceType.PRINTER -> R.string.home_appliance_filter_printer
        ApplianceType.SERVER -> R.string.home_appliance_filter_server

        // 🏭 Industriel
        ApplianceType.ELECTRIC_MOTOR -> R.string.home_appliance_filter_motor
        ApplianceType.PUMP -> R.string.home_appliance_filter_pump
        ApplianceType.ROBOTIC_ARM -> R.string.home_appliance_filter_robotic_arm
        ApplianceType.CONVEYOR_BELT -> R.string.home_appliance_filter_conveyor_belt
        ApplianceType.PACKING_MACHINE -> R.string.home_appliance_filter_packing_machine
        ApplianceType.FACTORY_MACHINE -> R.string.home_appliance_filter_factory_machine
        ApplianceType.COOLING_UNIT -> R.string.home_appliance_filter_cooling_unit
        ApplianceType.HVAC -> R.string.home_appliance_filter_hvac

        // 🏢 Infrastructure
        ApplianceType.ELEVATOR -> R.string.home_appliance_filter_elevator
        ApplianceType.ESCALATOR -> R.string.home_appliance_filter_escalator

        // ⚡ Énergie
        ApplianceType.SOLAR_PANEL -> R.string.home_appliance_filter_solar_panel
        ApplianceType.ELECTRIC_METER -> R.string.home_appliance_filter_electric_meter
        ApplianceType.GENERATOR -> R.string.home_appliance_filter_generator
        ApplianceType.POWER_SOURCE -> R.string.home_appliance_filter_power_source

        // 🧊 Commerce
        ApplianceType.SOFT_DRINK_MACHINE -> R.string.home_appliance_filter_soft_drink_machine
        ApplianceType.VENDING_MACHINE -> R.string.home_appliance_filter_vending_machine

        // 🏗️ Bâtiments
        ApplianceType.HOUSE -> R.string.home_appliance_filter_house
        ApplianceType.FARM -> R.string.home_appliance_filter_farm
        ApplianceType.SHOP -> R.string.home_appliance_filter_shop
        ApplianceType.OFFICE_BUILDING -> R.string.home_appliance_filter_office
        ApplianceType.FACTORY_BUILDING -> R.string.home_appliance_filter_factory_building
        ApplianceType.HOSPITAL -> R.string.home_appliance_filter_hospital
        ApplianceType.SKYSCRAPER -> R.string.home_appliance_filter_skyscraper

        ApplianceType.OTHER -> R.string.home_appliance_filter_other
    }


val ApplianceType.imageResId: Int
    get() = when (this) {

        ApplianceType.REFRIGERATOR -> R.drawable.frigo
        ApplianceType.FREEZER -> R.drawable.congelateur
        ApplianceType.WASHING_MACHINE -> R.drawable.machine_a_laver
        ApplianceType.DISHWASHER -> R.drawable.machine_a_vaisselle
        ApplianceType.MICROWAVE -> R.drawable.microwave
        ApplianceType.OVEN -> R.drawable.oven
        ApplianceType.COOKING_STOVE -> R.drawable.plaque_chauffante
        ApplianceType.TOASTER -> R.drawable.toaster
        ApplianceType.MIXER -> R.drawable.mixer
        ApplianceType.COFFEE_MAKER -> R.drawable.coffee_maker
        ApplianceType.IRON -> R.drawable.ironing
        ApplianceType.VACUUM_CLEANER -> R.drawable.vacuum_cleaner
        ApplianceType.HAIR_DRYER -> R.drawable.hairdryer
        ApplianceType.SHAVER -> R.drawable.shaver
        ApplianceType.FAN -> R.drawable.ventilateur
        ApplianceType.TOWER_FAN -> R.drawable.tower_fan
        ApplianceType.AIR_CONDITIONER -> R.drawable.climatiseur
        ApplianceType.RADIATOR -> R.drawable.radiator
        ApplianceType.ELECTRICAL_VEHICLE -> R.drawable.voiture_electrique

        ApplianceType.LIGHTING -> R.drawable.lamp

        ApplianceType.TV -> R.drawable.ecran_de_television
        ApplianceType.DESKTOP_COMPUTER -> R.drawable.ordinateur_de_bureau
        ApplianceType.LAPTOP_COMPUTER -> R.drawable.ordinateur_portable
        ApplianceType.PRINTER -> R.drawable.printer
        ApplianceType.SERVER -> R.drawable.servers

        ApplianceType.ELECTRIC_MOTOR -> R.drawable.moteur_electrique
        ApplianceType.PUMP -> R.drawable.pump
        ApplianceType.ROBOTIC_ARM -> R.drawable.robotic_arm
        ApplianceType.CONVEYOR_BELT -> R.drawable.conveyor_belt
        ApplianceType.PACKING_MACHINE -> R.drawable.packing_machine
        ApplianceType.FACTORY_MACHINE -> R.drawable.moteur_electrique
        ApplianceType.COOLING_UNIT -> R.drawable.air_conditioner_ventilator
        ApplianceType.HVAC -> R.drawable.air_conditioner_ventilator

        ApplianceType.ELEVATOR -> R.drawable.elevator
        ApplianceType.ESCALATOR -> R.drawable.escalator

        ApplianceType.SOLAR_PANEL -> R.drawable.cellule_photovoltaique
        ApplianceType.ELECTRIC_METER -> R.drawable.electric_meter
        ApplianceType.GENERATOR -> R.drawable.power
        ApplianceType.POWER_SOURCE -> R.drawable.power

        ApplianceType.SOFT_DRINK_MACHINE -> R.drawable.soft_drink_soda
        ApplianceType.VENDING_MACHINE -> R.drawable.soft_drink_soda

        ApplianceType.HOUSE -> R.drawable.maison
        ApplianceType.FARM -> R.drawable.farm_house
        ApplianceType.SHOP -> R.drawable.boutique
        ApplianceType.OFFICE_BUILDING -> R.drawable.immeuble_de_bureaux
        ApplianceType.FACTORY_BUILDING -> R.drawable.factory_building
        ApplianceType.HOSPITAL -> R.drawable.hospital_svgrepo_com
        ApplianceType.SKYSCRAPER -> R.drawable.skyscraper

        ApplianceType.OTHER -> R.drawable.electromenager
    }

val ApplianceHeatType.labelResId: Int
    get() = when (this) {
        ApplianceHeatType.HEATING -> R.string.appliance_heat_type_heating
        ApplianceHeatType.COOLING -> R.string.appliance_heat_type_cooling
        ApplianceHeatType.NON_THERMAL -> R.string.appliance_heat_type_non_heating
    }