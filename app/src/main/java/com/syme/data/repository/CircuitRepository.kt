package com.syme.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestoreException
import com.syme.domain.model.Circuit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CircuitRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun circuitCollection(
        userId: String,
        installationId: String
    ) = firestore
        .collection("users")
        .document(userId)
        .collection("installations")
        .document(installationId)
        .collection("circuits")

    // 🔁 Observe circuits (temps réel)
    fun observeCircuits(
        userId: String,
        installationId: String
    ): Flow<List<Circuit>> = callbackFlow {

        val listener: ListenerRegistration =
            circuitCollection(userId, installationId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("CircuitRepository", "Erreur observeCircuits", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val circuits = snapshot?.documents?.mapNotNull {
                        try {
                            it.toObject(Circuit::class.java)
                        } catch (e: Exception) {
                            Log.e("CircuitRepository", "Erreur conversion document -> Circuit", e)
                            null
                        }
                    } ?: emptyList()

                    trySend(circuits).isSuccess
                }

        awaitClose { listener.remove() }
    }

    // 📥 Get once
    suspend fun getAllOnce(userId: String, installationId: String): List<Circuit> {
        return try {
            circuitCollection(userId, installationId)
                .get()
                .await()
                .documents
                .mapNotNull {
                    try {
                        it.toObject(Circuit::class.java)
                    } catch (e: Exception) {
                        Log.e("CircuitRepository", "Erreur conversion document -> Circuit", e)
                        null
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            Log.e("CircuitRepository", "Erreur getAllOnce Firestore", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("CircuitRepository", "Erreur getAllOnce", e)
            emptyList()
        }
    }

    // ➕ Add circuit
    suspend fun addCircuit(userId: String, installationId: String, circuit: Circuit) {
        try {
            circuitCollection(userId, installationId)
                .document(circuit.circuitId.toString())
                .set(circuit)
                .await()
        } catch (e: Exception) {
            Log.e("CircuitRepository", "Erreur addCircuit", e)
            throw e
        }
    }

    // ✏️ Update circuit
    suspend fun updateCircuit(userId: String, installationId: String, circuit: Circuit) {
        try {
            circuitCollection(userId, installationId)
                .document(circuit.circuitId.toString())
                .set(circuit)
                .await()
        } catch (e: Exception) {
            Log.e("CircuitRepository", "Erreur updateCircuit", e)
            throw e
        }
    }

    // ❌ Delete circuit
    suspend fun deleteCircuit(userId: String, installationId: String, circuitId: String) {
        try {
            circuitCollection(userId, installationId)
                .document(circuitId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("CircuitRepository", "Erreur deleteCircuit", e)
            throw e
        }
    }
}