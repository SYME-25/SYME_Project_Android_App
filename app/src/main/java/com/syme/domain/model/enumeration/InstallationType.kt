package com.syme.domain.model.enumeration

/**
 * Categorises an installation for tariff resolution.
 *
 * [firestoreKey] maps directly to the Firestore document path:
 *   tariffs/{firestoreKey}/config
 */
enum class InstallationType(val firestoreKey: String) {
    RESIDENTIAL("residential"),   // Maison / appartement  — tarif social, faible puissance
    COMMERCIAL("commercial"),     // Boutique, bureau      — tarif intermédiaire, bonus demand
    INDUSTRIAL("industrial"),     // Usine                 — haute puissance, demand central
    AGRICULTURAL("agricultural"), // Ferme, élevage        — tarif préférentiel / saisonnier
    PUBLIC("public"),             // École, hôpital, mairie — TVA souvent 0 %, tarif réglementé
    OTHER("other")                // Fallback
}
