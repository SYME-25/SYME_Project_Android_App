package com.syme.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
                    Log.e("ConsumptionRepository", "Erreur observeAll", error)
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    try {
                        it.toObject<Consumption>()
                    } catch (e: Exception) {
                        Log.e("ConsumptionRepository", "Erreur conversion document -> Consumption", e)
                        null
                    }
                } ?: emptyList()

                trySend(list).isSuccess
            }

        awaitClose { listener.remove() }
    }

    // 📥 GET ONCE
    suspend fun getAllOnce(ownerId: String, installationId: String): List<Consumption> {
        return try {
            collection(ownerId, installationId)
                .get()
                .await()
                .documents
                .mapNotNull {
                    try {
                        it.toObject<Consumption>()
                    } catch (e: Exception) {
                        Log.e("ConsumptionRepository", "Erreur conversion document -> Consumption", e)
                        null
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            Log.e("ConsumptionRepository", "Erreur getAllOnce Firestore", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("ConsumptionRepository", "Erreur getAllOnce", e)
            emptyList()
        }
    }

    // ➕ INSERT
    suspend fun insert(ownerId: String, installationId: String, consumption: Consumption) {
        try {
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
        } catch (e: Exception) {
            Log.e("ConsumptionRepository", "Erreur insert", e)
            throw e
        }
    }

    // ✏️ UPDATE
    suspend fun update(ownerId: String, installationId: String, consumption: Consumption) {
        try {
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
        } catch (e: Exception) {
            Log.e("ConsumptionRepository", "Erreur update", e)
            throw e
        }
    }

    // ❌ DELETE
    suspend fun delete(ownerId: String, installationId: String, consumptionId: String) {
        try {
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
        } catch (e: Exception) {
            Log.e("ConsumptionRepository", "Erreur delete", e)
            throw e
        }
    }
}