package com.syme.utils

import com.syme.R
import com.syme.domain.model.Appliance
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.ApplianceType
import java.util.Locale

val applianceCatalog = listOf(

    // 🏠 Domestique
    Appliance("1", "","Refrigerator", ApplianceType.REFRIGERATOR, ApplianceHeatType.COOLING, false, 150f,
        metadata = mapOf("description" to R.string.home_appliance_desc_refrigerator)),

    Appliance("2", "","Freezer", ApplianceType.FREEZER, ApplianceHeatType.COOLING, false, 200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_freezer)),

    Appliance("3", "","Washing Machine", ApplianceType.WASHING_MACHINE, ApplianceHeatType.NON_THERMAL, false, 500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_washing_machine)),

    Appliance("4", "","Dishwasher", ApplianceType.DISHWASHER, ApplianceHeatType.HEATING, false, 1200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_dishwasher)),

    Appliance("5", "","Microwave", ApplianceType.MICROWAVE, ApplianceHeatType.HEATING, false, 1000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_microwave)),

    Appliance("6", "","Oven", ApplianceType.OVEN, ApplianceHeatType.HEATING, false, 2000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_oven)),

    Appliance("7", "","Cooking Stove", ApplianceType.COOKING_STOVE, ApplianceHeatType.HEATING, false, 1800f,
        metadata = mapOf("description" to R.string.home_appliance_desc_cooking_stove)),

    Appliance("8", "","Toaster", ApplianceType.TOASTER, ApplianceHeatType.HEATING, false, 800f,
        metadata = mapOf("description" to R.string.home_appliance_desc_toaster)),

    Appliance("9", "","Mixer", ApplianceType.MIXER, ApplianceHeatType.NON_THERMAL, false, 300f,
        metadata = mapOf("description" to R.string.home_appliance_desc_mixer)),

    Appliance("10", "","Coffee Maker", ApplianceType.COFFEE_MAKER, ApplianceHeatType.HEATING, false, 900f,
        metadata = mapOf("description" to R.string.home_appliance_desc_coffee_maker)),

    Appliance("11", "","Iron", ApplianceType.IRON, ApplianceHeatType.HEATING, false, 1200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_iron)),

    Appliance("12", "","Vacuum Cleaner", ApplianceType.VACUUM_CLEANER, ApplianceHeatType.NON_THERMAL, false, 1000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_vacuum)),

    Appliance("13", "","Hair Dryer", ApplianceType.HAIR_DRYER, ApplianceHeatType.HEATING, false, 1500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_hair_dryer)),

    Appliance("14", "","Electric Shaver", ApplianceType.SHAVER, ApplianceHeatType.NON_THERMAL, false, 100f,
        metadata = mapOf("description" to R.string.home_appliance_desc_shaver)),

    Appliance("15", "","Fan", ApplianceType.FAN, ApplianceHeatType.NON_THERMAL, false, 70f,
        metadata = mapOf("description" to R.string.home_appliance_desc_fan)),

    Appliance("16", "","Tower Fan", ApplianceType.TOWER_FAN, ApplianceHeatType.NON_THERMAL, false, 90f,
        metadata = mapOf("description" to R.string.home_appliance_desc_tower_fan)),

    Appliance("17", "","Air Conditioner", ApplianceType.AIR_CONDITIONER, ApplianceHeatType.COOLING, false, 1500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_air_conditioner)),

    Appliance("18", "","Radiator", ApplianceType.RADIATOR, ApplianceHeatType.HEATING, false, 2000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_radiator)),

    Appliance("19", "","Electric Vehicle", ApplianceType.ELECTRICAL_VEHICLE, ApplianceHeatType.NON_THERMAL, false, 2500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_electric_vehicle)),

    // 💡
    Appliance("20","", "Lighting", ApplianceType.LIGHTING, ApplianceHeatType.NON_THERMAL, false, 60f,
        metadata = mapOf("description" to R.string.home_appliance_desc_lighting)),

    // 💻
    Appliance("21","", "Television", ApplianceType.TV, ApplianceHeatType.NON_THERMAL, false, 120f,
        metadata = mapOf("description" to R.string.home_appliance_desc_tv)),

    Appliance("22","", "Desktop Computer", ApplianceType.DESKTOP_COMPUTER, ApplianceHeatType.NON_THERMAL, false, 300f,
        metadata = mapOf("description" to R.string.home_appliance_desc_desktop_computer)),

    Appliance("23","", "Laptop Computer", ApplianceType.LAPTOP_COMPUTER, ApplianceHeatType.NON_THERMAL, false, 150f,
        metadata = mapOf("description" to R.string.home_appliance_desc_laptop_computer)),

    Appliance("24","", "Printer", ApplianceType.PRINTER, ApplianceHeatType.NON_THERMAL, false, 500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_printer)),

    Appliance("25", "","Server", ApplianceType.SERVER, ApplianceHeatType.NON_THERMAL, false, 800f,
        metadata = mapOf("description" to R.string.home_appliance_desc_server)),

    // 🏭
    Appliance("26", "","Electric Motor", ApplianceType.ELECTRIC_MOTOR, ApplianceHeatType.NON_THERMAL, false, 1000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_motor)),

    Appliance("27","", "Pump", ApplianceType.PUMP, ApplianceHeatType.NON_THERMAL, false, 750f,
        metadata = mapOf("description" to R.string.home_appliance_desc_pump)),

    Appliance("28","", "Robotic Arm", ApplianceType.ROBOTIC_ARM, ApplianceHeatType.NON_THERMAL, false, 1200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_robotic_arm)),

    Appliance("29", "","Conveyor Belt", ApplianceType.CONVEYOR_BELT, ApplianceHeatType.NON_THERMAL, false, 1500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_conveyor_belt)),

    Appliance("30","", "Packing Machine", ApplianceType.PACKING_MACHINE, ApplianceHeatType.NON_THERMAL, false, 2000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_packing_machine)),

    Appliance("31","", "Industrial Machine", ApplianceType.FACTORY_MACHINE, ApplianceHeatType.NON_THERMAL, false, 2500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_factory_machine)),

    Appliance("32", "","HVAC", ApplianceType.HVAC, ApplianceHeatType.COOLING, false, 3000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_hvac)),

    // 🏢
    Appliance("33","", "Elevator", ApplianceType.ELEVATOR, ApplianceHeatType.NON_THERMAL, false, 4000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_elevator)),

    Appliance("34","", "Escalator", ApplianceType.ESCALATOR, ApplianceHeatType.NON_THERMAL, false, 3500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_escalator)),

    // ⚡
    Appliance("35","", "Solar Panel", ApplianceType.SOLAR_PANEL, ApplianceHeatType.NON_THERMAL, false, 300f,
        metadata = mapOf("description" to R.string.home_appliance_desc_solar_panel)),

    Appliance("36", "","Generator", ApplianceType.GENERATOR, ApplianceHeatType.HEATING, false, 5000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_generator)),

    // 🧊
    Appliance("37","", "Soft Drink Machine", ApplianceType.SOFT_DRINK_MACHINE, ApplianceHeatType.COOLING, false, 600f,
        metadata = mapOf("description" to R.string.home_appliance_desc_soft_drink_machine)),

    Appliance("38", "","Other", ApplianceType.OTHER, ApplianceHeatType.NON_THERMAL, false, 500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_other))
).sortedBy { it.applianceId.toInt() }
