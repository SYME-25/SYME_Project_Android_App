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

    // Collection des measurements pour un meter spécifique
    private fun collection(userId: String, installationId: String, meterId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("meters")
            .document(meterId)
            .collection("measurements")

    // Observe les mesures en temps réel pour un meter
    fun observeRealtime(userId: String, installationId: String, meterId: String): Flow<List<Measurement>> = callbackFlow {
        val listener = collection(userId, installationId, meterId)
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

    // Récupération historique
    suspend fun getHistorical(userId: String, installationId: String, meterId: String, limit: Int = 100): List<Measurement> =
        collection(userId, installationId, meterId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<Measurement>() }
}
