package com.syme.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.syme.domain.model.TariffConfig
import com.syme.domain.model.TariffOverride
import com.syme.domain.model.enumeration.InstallationType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Resolves the effective [TariffConfig] for an installation using a two-layer strategy:
 *
 *  1. **Global tariff** — `tariffs/{installationType}/config/current`
 *     Set by the SYME admin, shared across all installations of the same type.
 *
 *  2. **Per-installation override** — `users/{uid}/installations/{id}/tariff/override`
 *     Optional. Only non-null fields replace the global values.
 *     Also set by the SYME admin — read-only from the client app.
 *
 * The resolved config has [TariffConfig.installationType] set as a snapshot
 * for invoice traceability.
 */
class TariffRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ── Firestore paths ──────────────────────────────────────────────────────

    private fun globalDoc(type: InstallationType) =
        firestore.collection("tariffs")
            .document(type.firestoreKey)
            .collection("config")
            .document("current")

    private fun overrideDoc(ownerId: String, installationId: String) =
        firestore.collection("users")
            .document(ownerId)
            .collection("installations")
            .document(installationId)
            .collection("tariff")
            .document("override")

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * One-shot read of the resolved tariff for a given installation.
     * Used by [AutoBillingOrchestrator] at compute time.
     *
     * Returns null if no global tariff is configured for this [InstallationType].
     */
    suspend fun getOnce(
        ownerId: String,
        installationId: String,
        installationType: InstallationType
    ): TariffConfig? {
        val global = try {
            globalDoc(installationType)
                .get()
                .await()
                .toObject<TariffConfig>()
        } catch (e: Exception) {
            Log.e("TariffRepository", "Failed to fetch global tariff", e)
            null
        } ?: return null

        val override = try {
            overrideDoc(ownerId, installationId)
                .get()
                .await()
                .toObject<TariffOverride>()
        } catch (e: Exception) {
            Log.e("TariffRepository", "Failed to fetch override tariff", e)
            null
        }

        return resolve(global, override, installationType)
    }

    /**
     * Observes the resolved tariff in real-time.
     * Emits a new value whenever either the global tariff or the override changes.
     */
    fun observe(
        ownerId: String,
        installationId: String,
        installationType: InstallationType
    ): Flow<TariffConfig?> = callbackFlow {

        var global: TariffConfig? = null
        var override: TariffOverride? = null

        fun emit() {
            val base = global ?: return
            trySend(resolve(base, override, installationType))
        }

        val globalListener: ListenerRegistration? = try {
            globalDoc(installationType)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    global = try {
                        snap?.toObject<TariffConfig>()
                    } catch (e: Exception) {
                        Log.e("TariffRepository", "Global tariff conversion failed", e)
                        null
                    }
                    emit()
                }
        } catch (e: Exception) {
            Log.e("TariffRepository", "Global listener failed", e)
            null
        }

        val overrideListener: ListenerRegistration? = try {
            overrideDoc(ownerId, installationId)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    override = try {
                        snap?.toObject<TariffOverride>()
                    } catch (e: Exception) {
                        Log.e("TariffRepository", "Override tariff conversion failed", e)
                        null
                    }
                    emit()
                }
        } catch (e: Exception) {
            Log.e("TariffRepository", "Override listener failed", e)
            null
        }

        awaitClose {
            globalListener?.remove()
            overrideListener?.remove()
        }
    }

    /**
     * Saves the global tariff for a given [InstallationType].
     * Should only be called from an admin-authenticated context.
     */
    suspend fun saveGlobal(type: InstallationType, tariff: TariffConfig) {
        try {
            globalDoc(type).set(tariff).await()
        } catch (e: Exception) {
            Log.e("TariffRepository", "saveGlobal failed", e)
        }
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun resolve(
        global: TariffConfig,
        override: TariffOverride?,
        installationType: InstallationType
    ): TariffConfig {
        val merged = override?.applyTo(global) ?: global
        return merged.copy(installationType = installationType)
    }
}