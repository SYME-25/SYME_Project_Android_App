package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    fun observeCircuits(
        userId: String,
        installationId: String
    ): Flow<List<Circuit>> = callbackFlow {

        val listener: ListenerRegistration =
            circuitCollection(userId, installationId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val circuits = snapshot?.documents?.mapNotNull {
                        it.toObject(Circuit::class.java)
                    } ?: emptyList()

                    trySend(circuits)
                }

        awaitClose { listener.remove() }
    }

    suspend fun addCircuit(
        userId: String,
        installationId: String,
        circuit: Circuit
    ) {
        val docRef = circuitCollection(userId, installationId).document()
        val circuitWithId = circuit.copy(circuitId = docRef.id)

        docRef.set(circuitWithId).await()
    }

    suspend fun updateCircuit(
        userId: String,
        installationId: String,
        circuit: Circuit
    ) {
        circuitCollection(userId, installationId)
            .document(circuit.circuitId)
            .set(circuit)
            .await()
    }

    suspend fun deleteCircuit(
        userId: String,
        installationId: String,
        circuitId: String
    ) {
        circuitCollection(userId, installationId)
            .document(circuitId)
            .delete()
            .await()
    }
}
