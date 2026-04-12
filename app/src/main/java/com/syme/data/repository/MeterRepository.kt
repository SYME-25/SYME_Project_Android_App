package com.syme.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Measurement
import com.syme.domain.model.Meter
import com.syme.domain.model.Relay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeterRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase
) {

    private val globalMeters = firestore.collection("meters_global")
    private val relayMutex = Mutex()

    private fun installationMeters(userId: String, installationId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("meters")

    private fun collection(userId: String, installationId: String, meterId: String) =
        installationMeters(userId, installationId)
            .document(meterId)
            .collection("measurements")

    private fun realtimeRef(userId: String, installationId: String, meterId: String): DatabaseReference =
        realtimeDb.reference
            .child("realTimeMeasurements")
            .child(userId)
            .child(installationId)
            .child(meterId)

    fun observeMeters(userId: String, installationId: String): Flow<List<Meter>> = callbackFlow {
        val listener: ListenerRegistration? = try {
            installationMeters(userId, installationId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MeterRepository", "Erreur observeMeters", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    val meters = snapshot?.documents?.mapNotNull {
                        try {
                            it.toObject<Meter>()
                        } catch (e: Exception) {
                            Log.e("MeterRepository", "Conversion Meter failed", e)
                            null
                        }
                    } ?: emptyList()
                    trySend(meters).isSuccess
                }
        } catch (e: Exception) {
            Log.e("MeterRepository", "observeMeters listener failed", e)
            null
        }
        awaitClose { listener?.remove() }
    }

    private fun hashSecurityCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(code.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    suspend fun loadMeterToInstallation(
        userId: String,
        installationId: String,
        meterId: String,
        inputCodeHash: String
    ): Meter? {
        val localRef = installationMeters(userId, installationId).document(meterId)

        val localSnapshot = localRef.get().await()
        if (localSnapshot.exists()) {
            return localSnapshot.toObject<Meter>()
        }

        val globalSnapshot = globalMeters.document(meterId).get().await()
        val globalMeter = globalSnapshot.toObject<Meter>() ?: return null

        val metadata = globalMeter.metadata ?: return null

        val installedInstallationId = metadata["installedInstallationId"] as? String
        if (installedInstallationId != null && installedInstallationId != installationId) return null

        val storedHash = metadata["securityCodeHash"] as? String ?: return null
        if (storedHash != inputCodeHash) return null

        val relayCount = (metadata["relayCount"] as? Long ?: 0L).toInt()
        val relays = List(relayCount) { index -> Relay("R${index + 1}", meterId, index + 1) }

        val meterForInstallation = globalMeter.copy(
            installationId = installationId,
            relays = relays
        )

        localRef.set(meterForInstallation).await()
        globalMeters.document(meterId)
            .update("metadata.installedInstallationId", installationId)
            .await()

        return meterForInstallation
    }
    suspend fun updateRelayState(
        userId: String,
        installationId: String,
        meterId: String,
        relayId: String,
        newState: String
    ) = relayMutex.withLock {
        try {
            val meter = getMeter(userId, installationId, meterId) ?: return@withLock
            val updatedRelays = updateRelayInMeter(meter, relayId, newState)
            val relay = updatedRelays.first { it.relayId == relayId }

            val meterRtRef = ensureMeterExistsInRealtimeDb(userId, installationId, meterId)

            coroutineScope {
                val rtJob = async { updateRelayInRealtimeDb(meterRtRef, relay) }
                val firestoreJob = async { updateRelaysInFirestore(userId, installationId, meterId, updatedRelays) }
                val circuitsJob = async { updateCircuitsState(userId, installationId, meterId, relay.channel, newState) }
                rtJob.await()
                firestoreJob.await()
                circuitsJob.await()
            }
        } catch (e: Exception) {
            Log.e("MeterRepository", "Erreur updateRelayState", e)
            throw e
        }
    }

    private suspend fun getMeter(userId: String, installationId: String, meterId: String): Meter? =
        try {
            installationMeters(userId, installationId)
                .document(meterId)
                .get()
                .await()
                .toObject<Meter>()
        } catch (e: Exception) {
            Log.e("MeterRepository", "getMeter failed", e)
            null
        }

    private fun updateRelayInMeter(meter: Meter, relayId: String, newState: String): List<Relay> =
        meter.relays.map { if (it.relayId == relayId) it.copy(currentState = newState) else it }

    private suspend fun ensureMeterExistsInRealtimeDb(userId: String, installationId: String, meterId: String): DatabaseReference =
        realtimeDb.reference.child("realTimeMeters").child(userId).child(installationId).child(meterId)
            .also { ref ->
                try {
                    ref.updateChildren(mapOf("installationId" to installationId, "meterId" to meterId, "userId" to userId)).await()
                } catch (e: Exception) {
                    Log.e("MeterRepository", "ensureMeterExistsInRealtimeDb failed", e)
                }
            }

    private suspend fun updateRelayInRealtimeDb(meterRtRef: DatabaseReference, relay: Relay) {
        try {
            meterRtRef.child("relays").child(relay.relayId)
                .setValue(
                    mapOf(
                        "relayId" to relay.relayId,
                        "meterId" to relay.meterId,
                        "channel" to relay.channel,
                        "currentState" to relay.currentState,
                        "maxCurrent" to relay.maxCurrent
                    )
                ).await()
        } catch (e: Exception) {
            Log.e("MeterRepository", "updateRelayInRealtimeDb failed", e)
        }
    }

    private suspend fun updateRelaysInFirestore(userId: String, installationId: String, meterId: String, updatedRelays: List<Relay>) {
        try {
            installationMeters(userId, installationId)
                .document(meterId)
                .update("relays", updatedRelays)
                .await()
        } catch (e: Exception) {
            Log.e("MeterRepository", "updateRelaysInFirestore failed", e)
        }
    }

    private suspend fun updateCircuitsState(userId: String, installationId: String, meterId: String, channel: Int, newState: String) {
        try {
            val circuitsRef = firestore.collection("users").document(userId)
                .collection("installations")
                .document(installationId)
                .collection("circuits")

            val snapshot = circuitsRef.whereEqualTo("meterId", meterId)
                .whereEqualTo("relayChannel", channel)
                .get()
                .await()

            snapshot.documents.forEach { it.reference.update("currentState", newState).await() }
        } catch (e: Exception) {
            Log.e("MeterRepository", "updateCircuitsState failed", e)
        }
    }

    suspend fun saveToFirestore(userId: String, installationId: String, meterId: String, measurement: Measurement) {
        try {
            collection(userId, installationId, meterId)
                .document(measurement.timestamp.toString())
                .set(measurement)
                .await()
        } catch (e: Exception) {
            Log.e("MeterRepository", "saveToFirestore failed", e)
        }
    }

    fun observeRealtimeFromRealtimeDb(userId: String, installationId: String, meterId: String): Flow<List<Measurement>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Measurement::class.java) }
                trySend(list).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        realtimeRef(userId, installationId, meterId).orderByChild("timestamp").addValueEventListener(listener)
        awaitClose { realtimeRef(userId, installationId, meterId).removeEventListener(listener) }
    }

    fun observeAggregatedMeasurementsFromFirestore(userId: String, installationId: String): Flow<List<Measurement>> = callbackFlow {
        val listener: ListenerRegistration? = try {
            firestore.collectionGroup("measurements")
                .whereEqualTo("installationId", installationId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull {
                        try { it.toObject<Measurement>() } catch (e: Exception) { null }
                    } ?: emptyList()
                    trySend(list).isSuccess
                }
        } catch (e: Exception) {
            Log.e("MeterRepository", "observeAggregatedMeasurementsFromFirestore failed", e)
            null
        }
        awaitClose { listener?.remove() }
    }

    fun observeRelaysFromRealtimeDb(userId: String, installationId: String, meterId: String): Flow<List<Relay>> = callbackFlow {
        val ref = realtimeDb.reference.child("realTimeMeters").child(userId).child(installationId).child(meterId).child("relays")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val relays = snapshot.children.mapNotNull { it.getValue(Relay::class.java) }
                trySend(relays).isSuccess
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getAggregatedMeasurementsOnce(userId: String, installationId: String): List<Measurement> = coroutineScope {
        try {
            val metersSnapshot = firestore.collection("users").document(userId)
                .collection("installations")
                .document(installationId)
                .collection("meters")
                .get().await()

            val meterIds = metersSnapshot.documents.mapNotNull { it.id }

            val deferredMeasurements = meterIds.map { meterId ->
                async {
                    val snapshot = collection(userId, installationId, meterId).get().await()
                    snapshot.documents.mapNotNull { it.toObject<Measurement>() }
                }
            }
            deferredMeasurements.flatMap { it.await() }
        } catch (e: Exception) {
            Log.e("MeterRepository", "getAggregatedMeasurementsOnce failed", e)
            emptyList()
        }
    }
}