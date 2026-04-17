package com.syme.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Notification
import com.syme.domain.model.enumeration.NotificationCategory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ── Firestore ──────────────────────────────────────────
    private fun collection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("notifications")

    private fun document(userId: String, notificationId: String) =
        collection(userId).document(notificationId)

    // 🔁 OBSERVE ALL (temps réel)
    fun observeAll(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener: ListenerRegistration? = try {
            collection(userId)
                .orderBy("trace.createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationRepository", "observeAll listener error", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = try {
                        snapshot?.documents?.mapNotNull { it.toObject<Notification>() }
                            ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("NotificationRepository", "Conversion Notification failed", e)
                        emptyList()
                    }
                    trySend(list).isSuccess
                }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "observeAll failed", e)
            null
        }
        awaitClose { listener?.remove() }
    }

    // 🔁 OBSERVE UNREAD COUNT (temps réel)
    fun observeUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listener: ListenerRegistration? = try {
            collection(userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationRepository", "observeUnreadCount listener error", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.size() ?: 0).isSuccess
                }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "observeUnreadCount failed", e)
            null
        }
        awaitClose { listener?.remove() }
    }

    // 🔁 OBSERVE BY CATEGORY (temps réel)
    fun observeByCategory(
        userId: String,
        category: NotificationCategory
    ): Flow<List<Notification>> = callbackFlow {
        val listener: ListenerRegistration? = try {
            collection(userId)
                .whereEqualTo("category", category.name)
                .orderBy("trace.createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationRepository", "observeByCategory listener error", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = try {
                        snapshot?.documents?.mapNotNull { it.toObject<Notification>() }
                            ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("NotificationRepository", "Conversion Notification failed", e)
                        emptyList()
                    }
                    trySend(list).isSuccess
                }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "observeByCategory failed", e)
            null
        }
        awaitClose { listener?.remove() }
    }

    // ✅ MARK AS READ
    suspend fun markAsRead(userId: String, notificationId: String) {
        try {
            document(userId, notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "markAsRead failed", e)
        }
    }

    // ✅ MARK ALL AS READ
    suspend fun markAllAsRead(userId: String) {
        try {
            val unread = collection(userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            unread.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "markAllAsRead failed", e)
        }
    }

    // ❌ DELETE
    suspend fun delete(userId: String, notificationId: String) {
        try {
            document(userId, notificationId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "delete failed", e)
        }
    }

    // ❌ DELETE ALL
    suspend fun deleteAll(userId: String) {
        try {
            val all = collection(userId).get().await()
            val batch = firestore.batch()
            all.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "deleteAll failed", e)
        }
    }
}