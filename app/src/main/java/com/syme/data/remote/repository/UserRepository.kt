package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ── Firestore ──────────────────────────────────────────
    private fun document(userId: String) =
        firestore.collection("users")
            .document(userId)

    // 🔁 OBSERVE USER (temps réel)
    fun observe(userId: String): Flow<User?> = callbackFlow {

        val listener = document(userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject<User>()

                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    // 📥 GET ONCE
    suspend fun getOnce(userId: String): User? {
        return document(userId)
            .get()
            .await()
            .toObject()
    }

    // ✏️ UPDATE
    suspend fun update(user: User) {
        document(user.userId)
            .set(user)
            .await()
    }

    // ❌ DELETE
    suspend fun delete(userId: String) {
        document(userId)
            .delete()
            .await()
    }
}