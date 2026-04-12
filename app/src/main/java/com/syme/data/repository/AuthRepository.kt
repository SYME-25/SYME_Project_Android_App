package com.syme.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.syme.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(
        email: String,
        password: String,
        user: User
    ) {
        try {
            // Crée l'utilisateur dans Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUid = result.user?.uid
                ?: throw Exception("Firebase UID null")

            // Fusionne firebaseUid dans metadata pour ne pas écraser le reste
            val updatedMetadata = user.metadata?.toMutableMap() ?: mutableMapOf()
            updatedMetadata["firebaseUid"] = firebaseUid

            val userToSave = user.copy(metadata = updatedMetadata)

            // Sauvegarde Firestore sans écraser les autres champs
            firestore.collection("users")
                .document(user.userId)
                .set(userToSave, SetOptions.merge())
                .await()

        } catch (e: FirebaseFirestoreException) {
            Log.e("AuthRepository", "Erreur Firestore registerUser", e)
            throw e
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erreur registerUser", e)
            throw e
        }
    }

    suspend fun login(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Erreur login", e)
            throw e
        }
    }

    // ── Réinitialisation mot de passe ────────────────────────────────────────
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() {
        auth.signOut()
    }
}