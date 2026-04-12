package com.syme.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestoreException
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
                        Log.e("BillRepository", "Erreur observeBills", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val bills = snapshot?.documents?.mapNotNull {
                        try {
                            it.toObject(Bill::class.java)
                        } catch (e: Exception) {
                            Log.e("BillRepository", "Erreur conversion document -> Bill", e)
                            null
                        }
                    } ?: emptyList()

                    trySend(bills).isSuccess
                }

        awaitClose { listener.remove() }
    }

    // 📥 Get once
    suspend fun getBillsOnce(
        userId: String,
        installationId: String
    ): List<Bill> {
        return try {
            billCollection(userId, installationId)
                .get()
                .await()
                .documents
                .mapNotNull {
                    try {
                        it.toObject(Bill::class.java)
                    } catch (e: Exception) {
                        Log.e("BillRepository", "Erreur conversion document -> Bill", e)
                        null
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            Log.e("BillRepository", "Erreur getBillsOnce Firestore", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("BillRepository", "Erreur getBillsOnce", e)
            emptyList()
        }
    }

    // ➕ Add bill
    suspend fun addBill(
        userId: String,
        installationId: String,
        bill: Bill
    ) {
        try {
            billCollection(userId, installationId)
                .document(bill.billId)
                .set(bill)
                .await()
        } catch (e: Exception) {
            Log.e("BillRepository", "Erreur addBill", e)
            throw e
        }
    }

    // ✏️ Update bill
    suspend fun updateBill(
        userId: String,
        installationId: String,
        bill: Bill
    ) {
        try {
            billCollection(userId, installationId)
                .document(bill.billId)
                .set(bill)
                .await()
        } catch (e: Exception) {
            Log.e("BillRepository", "Erreur updateBill", e)
            throw e
        }
    }

    // ❌ Delete bill
    suspend fun deleteBill(
        userId: String,
        installationId: String,
        billId: String
    ) {
        try {
            billCollection(userId, installationId)
                .document(billId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("BillRepository", "Erreur deleteBill", e)
            throw e
        }
    }
}