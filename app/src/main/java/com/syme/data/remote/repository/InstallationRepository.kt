package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Installation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InstallationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun collection(ownerId: String) =
        require(ownerId.isNotBlank()) { "ownerId is blank!" }
            .let {
                firestore.collection("users")
                    .document(ownerId)
                    .collection("installations")
            }

    // 🔁 OBSERVE (temps réel)
    fun observeAll(ownerId: String): Flow<List<Installation>> = callbackFlow {
        val listener = collection(ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject<Installation>() }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    // 📥 GET ONCE
    suspend fun getAllOnce(ownerId: String): List<Installation> {
        return collection(ownerId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<Installation>() }
    }

    suspend fun getById(ownerId: String, id: String): Installation? {
        return collection(ownerId)
            .document(id)
            .get()
            .await()
            .toObject()
    }

    // ➕ CREATE
    suspend fun insert(ownerId: String, installation: Installation) {
        collection(ownerId)
            .document(installation.installationId)
            .set(installation)
            .await()
    }

    // ✏️ UPDATE
    suspend fun update(ownerId: String, installation: Installation) {
        collection(ownerId)
            .document(installation.installationId)
            .set(installation)
            .await()
    }

    // ❌ DELETE
    suspend fun delete(ownerId: String, installationId: String) {
        collection(ownerId)
            .document(installationId)
            .delete()
            .await()
    }
}
