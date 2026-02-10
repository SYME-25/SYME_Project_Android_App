package com.syme.utils

import com.syme.R
import com.syme.domain.model.Appliance
import com.syme.domain.model.enumeration.ApplianceHeatType
import com.syme.domain.model.enumeration.ApplianceType
import java.util.Locale

val applianceCatalog = listOf(

    // 🏠 Domestique
    Appliance("refrigerator", "","Refrigerator", ApplianceType.REFRIGERATOR, ApplianceHeatType.COOLING, false, 150f,
        metadata = mapOf("description" to R.string.home_appliance_desc_refrigerator)),

    Appliance("freezer", "","Freezer", ApplianceType.FREEZER, ApplianceHeatType.COOLING, false, 200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_freezer)),

    Appliance("washing_machine", "","Washing Machine", ApplianceType.WASHING_MACHINE, ApplianceHeatType.NON_THERMAL, false, 500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_washing_machine)),

    Appliance("dishwasher", "","Dishwasher", ApplianceType.DISHWASHER, ApplianceHeatType.HEATING, false, 1200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_dishwasher)),

    Appliance("microwave", "","Microwave", ApplianceType.MICROWAVE, ApplianceHeatType.HEATING, false, 1000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_microwave)),

    Appliance("oven", "","Oven", ApplianceType.OVEN, ApplianceHeatType.HEATING, false, 2000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_oven)),

    Appliance("cooking_stove", "","Cooking Stove", ApplianceType.COOKING_STOVE, ApplianceHeatType.HEATING, false, 1800f,
        metadata = mapOf("description" to R.string.home_appliance_desc_cooking_stove)),

    Appliance("toaster", "","Toaster", ApplianceType.TOASTER, ApplianceHeatType.HEATING, false, 800f,
        metadata = mapOf("description" to R.string.home_appliance_desc_toaster)),

    Appliance("mixer", "","Mixer", ApplianceType.MIXER, ApplianceHeatType.NON_THERMAL, false, 300f,
        metadata = mapOf("description" to R.string.home_appliance_desc_mixer)),

    Appliance("coffee_maker", "","Coffee Maker", ApplianceType.COFFEE_MAKER, ApplianceHeatType.HEATING, false, 900f,
        metadata = mapOf("description" to R.string.home_appliance_desc_coffee_maker)),

    Appliance("iron", "","Iron", ApplianceType.IRON, ApplianceHeatType.HEATING, false, 1200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_iron)),

    Appliance("vacuum", "","Vacuum Cleaner", ApplianceType.VACUUM_CLEANER, ApplianceHeatType.NON_THERMAL, false, 1000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_vacuum)),

    Appliance("hair_dryer", "","Hair Dryer", ApplianceType.HAIR_DRYER, ApplianceHeatType.HEATING, false, 1500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_hair_dryer)),

    Appliance("shaver", "","Electric Shaver", ApplianceType.SHAVER, ApplianceHeatType.NON_THERMAL, false, 100f,
        metadata = mapOf("description" to R.string.home_appliance_desc_shaver)),

    Appliance("fan", "","Fan", ApplianceType.FAN, ApplianceHeatType.NON_THERMAL, false, 70f,
        metadata = mapOf("description" to R.string.home_appliance_desc_fan)),

    Appliance("tower_fan", "","Tower Fan", ApplianceType.TOWER_FAN, ApplianceHeatType.NON_THERMAL, false, 90f,
        metadata = mapOf("description" to R.string.home_appliance_desc_tower_fan)),

    Appliance("air_conditioner", "","Air Conditioner", ApplianceType.AIR_CONDITIONER, ApplianceHeatType.COOLING, false, 1500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_air_conditioner)),

    Appliance("radiator", "","Radiator", ApplianceType.RADIATOR, ApplianceHeatType.HEATING, false, 2000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_radiator)),

    Appliance("electric_vehicle", "","Electric Vehicle", ApplianceType.ELECTRICAL_VEHICLE, ApplianceHeatType.NON_THERMAL, false, 2500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_electric_vehicle)),

    // 💡
    Appliance("lighting","", "Lighting", ApplianceType.LIGHTING, ApplianceHeatType.NON_THERMAL, false, 60f,
        metadata = mapOf("description" to R.string.home_appliance_desc_lighting)),

    // 💻
    Appliance("tv","", "Television", ApplianceType.TV, ApplianceHeatType.NON_THERMAL, false, 120f,
        metadata = mapOf("description" to R.string.home_appliance_desc_tv)),

    Appliance("desktop_computer","", "Desktop Computer", ApplianceType.DESKTOP_COMPUTER, ApplianceHeatType.NON_THERMAL, false, 300f,
        metadata = mapOf("description" to R.string.home_appliance_desc_desktop_computer)),

    Appliance("laptop_computer","", "Laptop Computer", ApplianceType.LAPTOP_COMPUTER, ApplianceHeatType.NON_THERMAL, false, 150f,
        metadata = mapOf("description" to R.string.home_appliance_desc_laptop_computer)),

    Appliance("printer","", "Printer", ApplianceType.PRINTER, ApplianceHeatType.NON_THERMAL, false, 500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_printer)),

    Appliance("server", "","Server", ApplianceType.SERVER, ApplianceHeatType.NON_THERMAL, false, 800f,
        metadata = mapOf("description" to R.string.home_appliance_desc_server)),

    // 🏭
    Appliance("motor", "","Electric Motor", ApplianceType.ELECTRIC_MOTOR, ApplianceHeatType.NON_THERMAL, false, 1000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_motor)),

    Appliance("pump","", "Pump", ApplianceType.PUMP, ApplianceHeatType.NON_THERMAL, false, 750f,
        metadata = mapOf("description" to R.string.home_appliance_desc_pump)),

    Appliance("robotic_arm","", "Robotic Arm", ApplianceType.ROBOTIC_ARM, ApplianceHeatType.NON_THERMAL, false, 1200f,
        metadata = mapOf("description" to R.string.home_appliance_desc_robotic_arm)),

    Appliance("conveyor_belt", "","Conveyor Belt", ApplianceType.CONVEYOR_BELT, ApplianceHeatType.NON_THERMAL, false, 1500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_conveyor_belt)),

    Appliance("packing_machine","", "Packing Machine", ApplianceType.PACKING_MACHINE, ApplianceHeatType.NON_THERMAL, false, 2000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_packing_machine)),

    Appliance("factory_machine","", "Industrial Machine", ApplianceType.FACTORY_MACHINE, ApplianceHeatType.NON_THERMAL, false, 2500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_factory_machine)),

    Appliance("hvac", "","HVAC", ApplianceType.HVAC, ApplianceHeatType.COOLING, false, 3000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_hvac)),

    // 🏢
    Appliance("elevator","", "Elevator", ApplianceType.ELEVATOR, ApplianceHeatType.NON_THERMAL, false, 4000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_elevator)),

    Appliance("escalator","", "Escalator", ApplianceType.ESCALATOR, ApplianceHeatType.NON_THERMAL, false, 3500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_escalator)),

    // ⚡
    Appliance("solar_panel","", "Solar Panel", ApplianceType.SOLAR_PANEL, ApplianceHeatType.NON_THERMAL, false, 300f,
        metadata = mapOf("description" to R.string.home_appliance_desc_solar_panel)),

    Appliance("generator", "","Generator", ApplianceType.GENERATOR, ApplianceHeatType.HEATING, false, 5000f,
        metadata = mapOf("description" to R.string.home_appliance_desc_generator)),

    // 🧊
    Appliance("soft_drink_machine","", "Soft Drink Machine", ApplianceType.SOFT_DRINK_MACHINE, ApplianceHeatType.COOLING, false, 600f,
        metadata = mapOf("description" to R.string.home_appliance_desc_soft_drink_machine)),

    Appliance("other", "","Other", ApplianceType.OTHER, ApplianceHeatType.NON_THERMAL, false, 500f,
        metadata = mapOf("description" to R.string.home_appliance_desc_other))
).sortedBy { it.applianceId.lowercase(Locale.getDefault()) }
