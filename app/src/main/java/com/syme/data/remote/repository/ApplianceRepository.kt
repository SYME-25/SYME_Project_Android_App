package com.syme.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Appliance
import com.syme.domain.model.Installation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ApplianceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun collection(ownerId: String, installationId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("installations")
            .document(installationId)
            .collection("appliances")

    private fun installationRef(ownerId: String, installationId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("installations")
            .document(installationId)

    private fun Appliance.energyWh(): Double =
        powerWatt * powerFactor * usageHoursPerDay.toDouble()

    // 🔁 OBSERVE
    fun observeAll(ownerId: String, installationId: String): Flow<List<Appliance>> = callbackFlow {
        val listener = collection(ownerId, installationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject<Appliance>() }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getAllOnce(ownerId: String, installationId: String): List<Appliance> {
        return collection(ownerId, installationId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<Appliance>() }
    }

    suspend fun getById(ownerId: String, installationId: String, id: String): Appliance? {
        return collection(ownerId, installationId)
            .document(id)
            .get()
            .await()
            .toObject()
    }

    // ➕ CREATE (recalcul global)
    suspend fun insert(ownerId: String, installationId: String, appliance: Appliance) {
        val applianceRef = collection(ownerId, installationId)
            .document(appliance.applianceId)

        val installationRef = installationRef(ownerId, installationId)

        val appliances = getAllOnce(ownerId, installationId) + appliance
        val totalEnergy = appliances.sumOf { it.energyWh() }

        firestore.runTransaction { transaction ->
            transaction.set(applianceRef, appliance)
            transaction.update(installationRef, "energyWh", totalEnergy.toInt())
        }.await()
    }

    // ✏️ UPDATE (recalcul global)
    suspend fun update(ownerId: String, installationId: String, appliance: Appliance) {
        val applianceRef = collection(ownerId, installationId)
            .document(appliance.applianceId)

        val installationRef = installationRef(ownerId, installationId)

        val appliances = getAllOnce(ownerId, installationId)
            .map { if (it.applianceId == appliance.applianceId) appliance else it }

        val totalEnergy = appliances.sumOf { it.energyWh() }

        firestore.runTransaction { transaction ->
            transaction.set(applianceRef, appliance)
            transaction.update(installationRef, "energyWh", totalEnergy)
        }.await()
    }

    // ❌ DELETE (recalcul global)
    suspend fun delete(ownerId: String, installationId: String, applianceId: String) {
        val applianceRef = collection(ownerId, installationId)
            .document(applianceId)

        val installationRef = installationRef(ownerId, installationId)

        val appliances = getAllOnce(ownerId, installationId)
            .filterNot { it.applianceId == applianceId }

        val totalEnergy = appliances.sumOf { it.energyWh() }

        firestore.runTransaction { transaction ->
            transaction.delete(applianceRef)
            transaction.update(installationRef, "energyWh", totalEnergy)
        }.await()
    }
}
