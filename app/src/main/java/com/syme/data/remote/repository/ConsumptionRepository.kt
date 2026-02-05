package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Consumption
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConsumptionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun collection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("consumptions")

    // 🔁 Observe consumptions in real-time
    fun observeAll(userId: String): Flow<List<Consumption>> = callbackFlow {
        val listener = collection(userId)
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

    // 📥 Get once
    suspend fun getAllOnce(userId: String): List<Consumption> =
        collection(userId).get().await().documents.mapNotNull { it.toObject<Consumption>() }

    // ➕ Insert
    suspend fun insert(userId: String, consumption: Consumption) {
        collection(userId)
            .document(consumption.consumptionId)
            .set(consumption)
            .await()
    }

    // ✏️ Update
    suspend fun update(userId: String, consumption: Consumption) {
        collection(userId)
            .document(consumption.consumptionId)
            .set(consumption)
            .await()
    }

    // ❌ Delete
    suspend fun delete(userId: String, consumptionId: String) {
        collection(userId)
            .document(consumptionId)
            .delete()
            .await()
    }
}
