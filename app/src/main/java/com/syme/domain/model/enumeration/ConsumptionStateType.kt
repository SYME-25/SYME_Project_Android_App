package com.syme.domain.model.enumeration

enum class ConsumptionStateType {
    RUNNING,    // En cours
    PAUSED,     // Temporisée ou interrompue
    STOPPED,    // Arrêtée
    WAITING,    // En attente
    COMPLETED,  // Terminé avec succès
    ERROR       // Terminé avec erreur
}