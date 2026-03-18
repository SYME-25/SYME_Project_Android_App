package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.syme.domain.model.Bill
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BillRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun billCollection(
        userId: String,
        installationId: String
    ) = firestore
        .collection("users")
        .document(userId)
        .collection("installations")
        .document(installationId)
        .collection("bills")

    // 🔁 Observe bills (temps réel)
    fun observeBills(
        userId: String,
        installationId: String
    ): Flow<List<Bill>> = callbackFlow {

        val listener: ListenerRegistration =
            billCollection(userId, installationId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val bills = snapshot?.documents?.mapNotNull {
                        it.toObject(Bill::class.java)
                    } ?: emptyList()

                    trySend(bills)
                }

        awaitClose { listener.remove() }
    }

    // 📥 Get once
    suspend fun getBillsOnce(
        userId: String,
        installationId: String
    ): List<Bill> {

        return billCollection(userId, installationId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Bill::class.java) }
    }

    // ➕ Add bill
    suspend fun addBill(
        userId: String,
        installationId: String,
        bill: Bill
    ) {

        val docRef = billCollection(userId, installationId)
            .document(bill.billId)

        docRef.set(bill).await()
    }

    // ✏️ Update bill
    suspend fun updateBill(
        userId: String,
        installationId: String,
        bill: Bill
    ) {

        billCollection(userId, installationId)
            .document(bill.billId)
            .set(bill)
            .await()
    }

    // ❌ Delete bill
    suspend fun deleteBill(
        userId: String,
        installationId: String,
        billId: String
    ) {

        billCollection(userId, installationId)
            .document(billId)
            .delete()
            .await()
    }
}