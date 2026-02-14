package com.syme.data.remote.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.Meter
import com.syme.domain.model.Relay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeterRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "MeterRepository"
    }

    // ---------------------
    // GLOBAL METERS
    // ---------------------
    private val globalMeters = firestore.collection("meters_global")

    // ---------------------
    // INSTALLATION METERS
    // ---------------------
    private fun installationMeters(userId: String, installationId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("installations")
            .document(installationId)
            .collection("meters")

    // ---------------------
    // OBSERVE METERS
    // ---------------------
    fun observeMeters(
        userId: String,
        installationId: String
    ): Flow<List<Meter>> = callbackFlow {

        Log.d(TAG, "observeMeters() called with userId=$userId, installationId=$installationId")

        val listener = installationMeters(userId, installationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeMeters() snapshot error", error)
                    close(error)
                    return@addSnapshotListener
                }

                val meters = snapshot?.documents
                    ?.mapNotNull { it.toObject<Meter>() }
                    ?: emptyList()

                Log.d(TAG, "observeMeters() -> received ${meters.size} meters")

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
        Log.d(TAG, "loadMeterToInstallation() called with meterId=$meterId, installationId=$installationId")

        val localRef = installationMeters(userId, installationId).document(meterId)

        val localSnapshot = localRef.get().await()
        if (localSnapshot.exists()) {
            Log.d(TAG, "Meter $meterId already exists in installation $installationId")
            return localSnapshot.toObject(Meter::class.java)
        }

        val globalSnapshot = globalMeters.document(meterId).get().await()
        val globalMeter = globalSnapshot.toObject(Meter::class.java) ?: run {
            Log.d(TAG, "Meter $meterId not found in global meters")
            return null
        }

        val metadata = globalMeter.metadata ?: run {
            Log.d(TAG, "Meter $meterId has no metadata")
            return null
        }

        val installedInstallationId = metadata["installedInstallationId"] as? String
        if (installedInstallationId != null && installedInstallationId != installationId) {
            Log.d(TAG, "Meter $meterId already installed in another installation $installedInstallationId")
            return null
        }

        val storedHash = metadata["securityCodeHash"] as? String ?: run {
            Log.d(TAG, "Meter $meterId has no stored security code")
            return null
        }

        // ❌ PLUS DE HASH ICI : compare directement le hash reçu
        if (storedHash != inputCodeHash) {
            Log.d(TAG, "Security code mismatch for meter $meterId, inputHash=$inputCodeHash, stored=$storedHash")
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

        Log.d(TAG, "Saving meter $meterId to installation $installationId")
        localRef.set(meterForInstallation).await()

        Log.d(TAG, "Marking meter $meterId as installed in global")
        globalMeters.document(meterId)
            .update("metadata.installedInstallationId", installationId)
            .await()

        Log.d(TAG, "Meter $meterId successfully loaded into installation $installationId")
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
    ) {
        val meterRef = installationMeters(userId, installationId).document(meterId)
        val meterSnapshot = meterRef.get().await()
        val meter = meterSnapshot.toObject(Meter::class.java) ?: run {
            Log.d(TAG, "updateRelayState() -> meter $meterId not found")
            return
        }

        val updatedRelays = meter.relays.map {
            if (it.relayId == relayId) it.copy(currentState = newState) else it
        }

        meterRef.update("relays", updatedRelays).await()
        Log.d(TAG, "updateRelayState() -> relay $relayId of meter $meterId updated to $newState")
    }
}
