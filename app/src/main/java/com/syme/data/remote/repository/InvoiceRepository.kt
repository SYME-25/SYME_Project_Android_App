package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Invoice
import com.syme.domain.model.enumeration.InvoiceStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Repository for invoices.
 *
 * Firestore layout:
 *   users/{ownerId}/installations/{installationId}/invoices/current        ← live invoice
 *   users/{ownerId}/installations/{installationId}/invoices/history/{id}   ← archived invoices
 *
 * "current" is a fixed document that is overwritten on every measurement.
 * When a subscription period ends, "current" is copied to "history/{invoiceId}"
 * and then deleted.
 */
class InvoiceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // ── Path helpers ────────────────────────────────────────────────────────

    private fun invoicesCol(ownerId: String, installationId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("installations")
            .document(installationId)
            .collection("invoices")

    private fun currentDoc(ownerId: String, installationId: String) =
        invoicesCol(ownerId, installationId).document("current")

    private fun historyCol(ownerId: String, installationId: String) =
        invoicesCol(ownerId, installationId)
            .document("history")
            .collection("entries")

    // ── Current invoice ─────────────────────────────────────────────────────

    /**
     * Writes (or overwrites) the current live invoice.
     * Called on every new aggregated measurement.
     */
    suspend fun upsertCurrent(ownerId: String, installationId: String, invoice: Invoice) {
        currentDoc(ownerId, installationId).set(invoice).await()
    }

    /** Reads the current invoice once. */
    suspend fun getCurrentOnce(ownerId: String, installationId: String): Invoice? =
        currentDoc(ownerId, installationId).get().await().toObject<Invoice>()

    /** Observes the current invoice in real-time. */
    fun observeCurrent(ownerId: String, installationId: String): Flow<Invoice?> = callbackFlow {
        val listener = currentDoc(ownerId, installationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObject<Invoice>())
            }
        awaitClose { listener.remove() }
    }

    // ── Archive ──────────────────────────────────────────────────────────────

    /**
     * Archives the current invoice into history and deletes the "current" document.
     * Should be called when a subscription period ends and a new one begins.
     *
     * Uses a Firestore batch to guarantee atomicity.
     */
    suspend fun archiveCurrent(ownerId: String, installationId: String) {
        val current = getCurrentOnce(ownerId, installationId) ?: return
        val archived = current.copy(status = InvoiceStatus.ISSUED)

        firestore.batch().apply {
            set(historyCol(ownerId, installationId).document(archived.invoiceId), archived)
            delete(currentDoc(ownerId, installationId))
        }.commit().await()
    }

    /** Observes the full invoice history in real-time (most recent first). */
    fun observeHistory(ownerId: String, installationId: String): Flow<List<Invoice>> =
        callbackFlow {
            val listener = historyCol(ownerId, installationId)
                .orderBy("issuedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val list = snapshot?.documents
                        ?.mapNotNull { it.toObject<Invoice>() }
                        ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }

    /** One-shot read of the full history. */
    suspend fun getHistoryOnce(ownerId: String, installationId: String): List<Invoice> =
        historyCol(ownerId, installationId)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .get().await()
            .documents.mapNotNull { it.toObject<Invoice>() }

    // ── Legacy helpers (kept for BillViewModel compatibility) ────────────────

    /**
     * Observes both current and history merged into a single list.
     * Current invoice is prepended at index 0 if it exists.
     */
    fun observeAll(ownerId: String, installationId: String): Flow<List<Invoice>> = callbackFlow {
        var current: Invoice? = null
        var history: List<Invoice> = emptyList()

        fun emit() = trySend((listOfNotNull(current) + history))

        val currentListener = currentDoc(ownerId, installationId)
            .addSnapshotListener { snap, _ ->
                current = snap?.toObject<Invoice>()
                emit()
            }

        val historyListener = historyCol(ownerId, installationId)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                history = snap?.documents?.mapNotNull { it.toObject<Invoice>() } ?: emptyList()
                emit()
            }

        awaitClose {
            currentListener.remove()
            historyListener.remove()
        }
    }

    // ── Status helpers ───────────────────────────────────────────────────────

    suspend fun markAsPaid(ownerId: String, installationId: String, invoiceId: String) {
        historyCol(ownerId, installationId)
            .document(invoiceId)
            .update("status", InvoiceStatus.PAID)
            .await()
    }

    suspend fun delete(ownerId: String, installationId: String, invoiceId: String) {
        historyCol(ownerId, installationId).document(invoiceId).delete().await()
    }
}
