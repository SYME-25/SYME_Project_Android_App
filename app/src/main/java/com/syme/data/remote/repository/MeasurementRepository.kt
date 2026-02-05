package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Measurement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MeasurementRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun collection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("measurements")

    // 🔁 Observe real-time measurements for meter
    fun observeRealtime(userId: String, meterId: String): Flow<List<Measurement>> = callbackFlow {
        val listener = collection(userId)
            .whereEqualTo("meterId", meterId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { it.toObject<Measurement>() } ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    // 📥 Get historical data once
    suspend fun getHistorical(userId: String, meterId: String, limit: Int = 100): List<Measurement> =
        collection(userId)
            .whereEqualTo("meterId", meterId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<Measurement>() }
}
