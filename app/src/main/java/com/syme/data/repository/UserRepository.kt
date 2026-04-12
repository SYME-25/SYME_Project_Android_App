package com.syme.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
        val listener: ListenerRegistration? = try {
            document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("UserRepository", "observe listener error", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val user = try {
                        snapshot?.toObject<User>()
                    } catch (e: Exception) {
                        Log.e("UserRepository", "Conversion User failed", e)
                        null
                    }

                    trySend(user).isSuccess
                }
        } catch (e: Exception) {
            Log.e("UserRepository", "observe failed", e)
            null
        }

        awaitClose { listener?.remove() }
    }

    // 📥 GET ONCE
    suspend fun getOnce(userId: String): User? {
        return try {
            document(userId)
                .get()
                .await()
                .toObject()
        } catch (e: Exception) {
            Log.e("UserRepository", "getOnce failed", e)
            null
        }
    }

    // ✏️ UPDATE
    suspend fun update(user: User) {
        try {
            document(user.userId)
                .set(user)
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "update failed", e)
        }
    }

    // ❌ DELETE
    suspend fun delete(userId: String) {
        try {
            document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "delete failed", e)
        }
    }

    suspend fun addFcmToken(userId: String, token: String) {
        try {
            document(userId)
                .update("fcmTokens", FieldValue.arrayUnion(token))
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "addFcmToken failed", e)
        }
    }

    suspend fun removeFcmToken(userId: String, token: String) {
        try {
            document(userId)
                .update("fcmTokens", FieldValue.arrayRemove(token))
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "removeFcmToken failed", e)
        }
    }
}