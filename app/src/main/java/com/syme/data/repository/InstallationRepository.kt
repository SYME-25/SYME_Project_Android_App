package com.syme.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Installation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InstallationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase
) {

    // ── Firestore ──────────────────────────────────────────
    private fun collection(ownerId: String) =
        require(ownerId.isNotBlank()) { "ownerId is blank!" }
            .let {
                firestore.collection("users")
                    .document(ownerId)
                    .collection("installations")
            }

    // ── Realtime Database ──────────────────────────────────
    private fun realtimeRef(ownerId: String) =
        realtimeDb.reference
            .child("realTimeInstallations")
            .child(ownerId)

    // 🔁 OBSERVE (temps réel)
    fun observeAll(ownerId: String): Flow<List<Installation>> = callbackFlow {
        val listener = collection(ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("InstallationRepository", "Erreur observeAll", error)
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    try {
                        it.toObject<Installation>()
                    } catch (e: Exception) {
                        Log.e("InstallationRepository", "Erreur conversion document -> Installation", e)
                        null
                    }
                } ?: emptyList()

                trySend(list).isSuccess
            }

        awaitClose { listener.remove() }
    }

    // 📥 GET ONCE
    suspend fun getAllOnce(ownerId: String): List<Installation> {
        return try {
            collection(ownerId)
                .get()
                .await()
                .documents
                .mapNotNull {
                    try {
                        it.toObject<Installation>()
                    } catch (e: Exception) {
                        Log.e("InstallationRepository", "Erreur conversion document -> Installation", e)
                        null
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            Log.e("InstallationRepository", "Erreur getAllOnce Firestore", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("InstallationRepository", "Erreur getAllOnce", e)
            emptyList()
        }
    }

    suspend fun getById(ownerId: String, id: String): Installation? {
        return try {
            collection(ownerId)
                .document(id)
                .get()
                .await()
                .toObject<Installation>()
        } catch (e: Exception) {
            Log.e("InstallationRepository", "Erreur getById", e)
            null
        }
    }

    // ➕ CREATE
    suspend fun insert(ownerId: String, installation: Installation) {
        try {
            collection(ownerId)
                .document(installation.installationId)
                .set(installation)
                .await()

            realtimeRef(ownerId)
                .child(installation.installationId)
                .setValue(installation)
                .await()
        } catch (e: Exception) {
            Log.e("InstallationRepository", "Erreur insert", e)
            throw e
        }
    }

    // ✏️ UPDATE
    suspend fun update(ownerId: String, installation: Installation) {
        try {
            collection(ownerId)
                .document(installation.installationId)
                .set(installation)
                .await()

            realtimeRef(ownerId)
                .child(installation.installationId)
                .setValue(installation)
                .await()
        } catch (e: Exception) {
            Log.e("InstallationRepository", "Erreur update", e)
            throw e
        }
    }

    // ❌ DELETE
    suspend fun delete(ownerId: String, installationId: String) {
        try {
            collection(ownerId)
                .document(installationId)
                .delete()
                .await()

            realtimeRef(ownerId)
                .child(installationId)
                .removeValue()
                .await()
        } catch (e: Exception) {
            Log.e("InstallationRepository", "Erreur delete", e)
            throw e
        }
    }
}