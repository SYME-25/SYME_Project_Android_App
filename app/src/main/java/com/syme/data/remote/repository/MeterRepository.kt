package com.syme.data.remote.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Measurement
import com.syme.domain.model.Meter
import com.syme.domain.model.Relay
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
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

    // ---------------------
    // GLOBAL METERS
    // ---------------------
    private val globalMeters = firestore.collection("meters_global")

    private val relayMutex = Mutex()

    // ---------------------
    // INSTALLATION METERS
    // ---------------------
    private fun installationMeters(userId: String, installationId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("meters")


    // ---------------- MEASUREMENTS FIRESTORE ----------------
    private fun collection(userId: String, installationId: String, meterId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("meters")
            .document(meterId)
            .collection("measurements")

    // ---------------- MEASUREMENTS REALTIME DATABASE ----------------
    private fun realtimeRef(
        userId: String,
        installationId: String,
        meterId: String
    ): DatabaseReference =
        realtimeDb.reference
            .child("realTimeMeasurements")
            .child(userId)
            .child(installationId)
            .child(meterId)


    // ---------------------
    // OBSERVE METERS
    // ---------------------
    fun observeMeters(
        userId: String,
        installationId: String
    ): Flow<List<Meter>> = callbackFlow {

        val listener = installationMeters(userId, installationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val meters = snapshot?.documents
                    ?.mapNotNull { it.toObject<Meter>() }
                    ?: emptyList()

                trySend(meters)
            }

        awaitClose { listener.remove() }
    }

    // ---------------------
    // HASH FUNCTION (SHA-256)
    // ---------------------
    private fun hashSecurityCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(code.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // ---------------------
    // LOAD METER
    // ---------------------
    suspend fun loadMeterToInstallation(
        userId: String,
        installationId: String,
        meterId: String,
        inputCodeHash: String // maintenant c'est déjà le hash
    ): Meter? {

        val localRef = installationMeters(userId, installationId).document(meterId)

        val localSnapshot = localRef.get().await()
        if (localSnapshot.exists()) {
            return localSnapshot.toObject(Meter::class.java)
        }

        val globalSnapshot = globalMeters.document(meterId).get().await()
        val globalMeter = globalSnapshot.toObject(Meter::class.java) ?: run {
            return null
        }

        val metadata = globalMeter.metadata ?: run {
            return null
        }

        val installedInstallationId = metadata["installedInstallationId"] as? String
        if (installedInstallationId != null && installedInstallationId != installationId) {
            return null
        }

        val storedHash = metadata["securityCodeHash"] as? String ?: run {
            return null
        }

        // ❌ PLUS DE HASH ICI : compare directement le hash reçu
        if (storedHash != inputCodeHash) {
            return null
        }

        // création des relays et sauvegarde inchangée
        val relayCount = (metadata["relayCount"] as? Long ?: 0L).toInt()
        val relays = List(relayCount) { index ->
            Relay("R${index + 1}", meterId, index + 1)
        }

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

    // ---------------------
    // UPDATE RELAY
    // ---------------------
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

            val meterRtRef = ensureMeterExistsInRealtimeDb(
                userId, installationId, meterId
            )

            coroutineScope {

                val rtJob = async {
                    updateRelayInRealtimeDb(meterRtRef, relay)
                }

                val firestoreMeterJob = async {
                    updateRelaysInFirestore(
                        userId,
                        installationId,
                        meterId,
                        updatedRelays
                    )
                }

                val circuitsJob = async {
                    updateCircuitsState(
                        userId,
                        installationId,
                        meterId,
                        relay.channel,
                        newState
                    )
                }

                // on attend que tout soit terminé
                rtJob.await()
                firestoreMeterJob.await()
                circuitsJob.await()
            }

        } catch (e: Exception) {
            throw e // important pour que le ViewModel puisse réagir
        }
    }
    private suspend fun getMeter(
        userId: String,
        installationId: String,
        meterId: String
    ): Meter? {
        val ref = installationMeters(userId, installationId).document(meterId)
        return ref.get().await().toObject(Meter::class.java)
    }

    private fun updateRelayInMeter(
        meter: Meter,
        relayId: String,
        newState: String
    ): List<Relay> =
        meter.relays.map {
            if (it.relayId == relayId)
                it.copy(currentState = newState)
            else it
        }

    private suspend fun ensureMeterExistsInRealtimeDb(
        userId: String,
        installationId: String,
        meterId: String
    ) = realtimeDb.reference
        .child("realTimeMeters")
        .child(userId)
        .child(installationId)
        .child(meterId)
        .also { ref ->
            ref.updateChildren(
                mapOf(
                    "installationId" to installationId,
                    "meterId" to meterId,
                    "userId" to userId
                )
            ).await()
        }

    private suspend fun updateRelayInRealtimeDb(
        meterRtRef: DatabaseReference,
        relay: Relay
    ) {
        meterRtRef.child("relays")
            .child(relay.relayId)
            .setValue(
                mapOf(
                    "relayId" to relay.relayId,
                    "meterId" to relay.meterId,
                    "channel" to relay.channel,
                    "currentState" to relay.currentState,
                    "maxCurrent" to relay.maxCurrent
                )
            )
            .await()
    }

    private suspend fun updateRelaysInFirestore(
        userId: String,
        installationId: String,
        meterId: String,
        updatedRelays: List<Relay>
    ) {
        installationMeters(userId, installationId)
            .document(meterId)
            .update("relays", updatedRelays)
            .await()
    }

    private suspend fun updateCircuitsState(
        userId: String,
        installationId: String,
        meterId: String,
        channel: Int,
        newState: String
    ) {
        val circuitsRef = firestore
            .collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("circuits")

        val snapshot = circuitsRef
            .whereEqualTo("meterId", meterId)
            .whereEqualTo("relayChannel", channel)
            .get()
            .await()

        snapshot.documents.forEach {
            it.reference.update("currentState", newState).await()
        }
    }

    //MEASUREMENTS
    suspend fun saveToFirestore(
        userId: String,
        installationId: String,
        meterId: String,
        measurement: Measurement
    ) {
        collection(userId, installationId, meterId)
            .document(measurement.timestamp.toString())
            .set(measurement)
            .await()
    }

    fun observeRealtimeFromRealtimeDb(
        userId: String,
        installationId: String,
        meterId: String
    ): Flow<List<Measurement>> = callbackFlow {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(Measurement::class.java)
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        realtimeRef(userId, installationId, meterId)
            .orderByChild("timestamp")
            .addValueEventListener(listener)

        awaitClose {
            realtimeRef(userId, installationId, meterId)
                .removeEventListener(listener)
        }
    }

    fun observeAggregatedMeasurementsFromFirestore(
        userId: String,
        installationId: String
    ): Flow<List<Measurement>> = callbackFlow {

        val listener = firestore
            .collectionGroup("measurements")
            .whereEqualTo("installationId", installationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject<Measurement>() }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun observeRelaysFromRealtimeDb(
        userId: String,
        installationId: String,
        meterId: String
    ): Flow<List<Relay>> = callbackFlow {
        val ref = realtimeDb.reference
            .child("realTimeMeters")
            .child(userId)
            .child(installationId)
            .child(meterId)
            .child("relays")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val relays = snapshot.children.mapNotNull { child ->
                    child.getValue(Relay::class.java)
                }
                trySend(relays)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getAggregatedMeasurementsOnce(
        userId: String,
        installationId: String
    ): List<Measurement> = coroutineScope {

        // 1️⃣ Liste tous les meters de l'installation
        val metersSnapshot = firestore
            .collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("meters")
            .get()
            .await()

        val meterIds = metersSnapshot.documents.mapNotNull { it.id }

        // 2️⃣ Pour chaque meter, lance une coroutine async pour lire les measurements
        val deferredMeasurements = meterIds.map { meterId ->
            async {
                val snapshot = collection(userId, installationId, meterId).get().await()
                snapshot.documents.mapNotNull { it.toObject(Measurement::class.java) }
            }
        }

        // 3️⃣ Attend toutes les coroutines et fusionne les résultats
        deferredMeasurements.flatMap { it.await() }
    }

}
