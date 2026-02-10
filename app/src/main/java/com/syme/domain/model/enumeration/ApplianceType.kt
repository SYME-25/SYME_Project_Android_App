package com.syme.domain.model.enumeration
enum class ApplianceType {

    // 🏠 Résidentiel & domestique
    REFRIGERATOR,        // frigo
    FREEZER,             // congélateur
    WASHING_MACHINE,     // machine à laver
    DISHWASHER,          // lave-vaisselle
    MICROWAVE,           // micro-ondes
    OVEN,                // four
    COOKING_STOVE,       // plaque chauffante
    TOASTER,             // grille-pain
    MIXER,               // mixeur
    COFFEE_MAKER,        // cafetière
    IRON,                // fer à repasser
    VACUUM_CLEANER,      // aspirateur
    HAIR_DRYER,          // sèche-cheveux
    SHAVER,              // rasoir électrique
    FAN,                 // ventilateur
    TOWER_FAN,           // ventilateur colonne
    AIR_CONDITIONER,     // climatiseur
    RADIATOR,            // radiateur électrique
    ELECTRICAL_VEHICLE,        // voiture electrique

    // 💡 Éclairage & prises
    LIGHTING,            // ampoule, lampe

    // 📺 Multimédia & informatique
    TV,                  // télévision
    DESKTOP_COMPUTER,    // ordinateur de bureau
    LAPTOP_COMPUTER,     // ordinateur portable
    PRINTER,             // imprimante
    SERVER,              // serveurs

    // 🏭 Industriel & machines
    ELECTRIC_MOTOR,      // moteur électrique
    PUMP,                // pompe
    ROBOTIC_ARM,         // bras robotisé
    CONVEYOR_BELT,       // tapis roulant
    PACKING_MACHINE,     // machine d’emballage
    FACTORY_MACHINE,     // machine industrielle générique
    COOLING_UNIT,        // unité de refroidissement
    HVAC,                // système HVAC

    // 🏢 Infrastructure & transport
    ELEVATOR,            // ascenseur
    ESCALATOR,           // escalator

    // ⚡ Énergie & production
    SOLAR_PANEL,         // panneau photovoltaïque
    ELECTRIC_METER,      // compteur électrique
    GENERATOR,           // générateur / groupe électrogène
    POWER_SOURCE,        // source d’énergie

    // 🧊 Commerce & divers
    SOFT_DRINK_MACHINE,  // distributeur boisson
    VENDING_MACHINE,     // distributeur générique

    // 🏗️ Bâtiments comme entités consommatrices
    HOUSE,
    FARM,
    SHOP,
    OFFICE_BUILDING,
    FACTORY_BUILDING,
    HOSPITAL,
    SKYSCRAPER,

    OTHER
}
