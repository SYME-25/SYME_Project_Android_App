package com.syme.data.remote.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Consumption
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConsumptionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase
) {

    // ── Firestore ──────────────────────────────────────────
    private fun collection(ownerId: String, installationId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("installations")
            .document(installationId)
            .collection("consumptions")

    // ── Realtime Database ──────────────────────────────────
    private fun realtimeRef(ownerId: String, installationId: String) =
        realtimeDb.reference
            .child("realTimeConsumptions")
            .child(ownerId)
            .child(installationId)

    // 🔁 OBSERVE
    fun observeAll(ownerId: String, installationId: String): Flow<List<Consumption>> = callbackFlow {
        val listener = collection(ownerId, installationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject<Consumption>() }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    // 📥 GET ONCE
    suspend fun getAllOnce(ownerId: String, installationId: String): List<Consumption> =
        collection(ownerId, installationId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<Consumption>() }

    // ➕ INSERT
    suspend fun insert(ownerId: String, installationId: String, consumption: Consumption) {

        // Firestore
        collection(ownerId, installationId)
            .document(consumption.consumptionId)
            .set(consumption)
            .await()

        // Realtime
        realtimeRef(ownerId, installationId)
            .child(consumption.consumptionId)
            .setValue(consumption)
            .await()
    }

    // ✏️ UPDATE
    suspend fun update(ownerId: String, installationId: String, consumption: Consumption) {

        // Firestore
        collection(ownerId, installationId)
            .document(consumption.consumptionId)
            .set(consumption)
            .await()

        // Realtime
        realtimeRef(ownerId, installationId)
            .child(consumption.consumptionId)
            .setValue(consumption)
            .await()
    }

    // ❌ DELETE
    suspend fun delete(ownerId: String, installationId: String, consumptionId: String) {

        // Firestore
        collection(ownerId, installationId)
            .document(consumptionId)
            .delete()
            .await()

        // Realtime
        realtimeRef(ownerId, installationId)
            .child(consumptionId)
            .removeValue()
            .await()
    }
}
