package com.syme.data.remote.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.syme.data.remote.model.UserFirebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(
        email: String,
        password: String,
        user: UserFirebase
    ) {
        // Crée l'utilisateur dans Firebase Auth
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUid = result.user?.uid ?: throw Exception("Firebase UID null")

        // Fusionne firebaseUid dans metadata pour ne pas écraser le reste
        val updatedMetadata = user.metadata.toMutableMap()
        updatedMetadata["firebaseUid"] = firebaseUid

        val userToSave = user.copy(metadata = updatedMetadata)

        // 🔹 set avec merge = ne pas écraser les champs existants
        firestore.collection("users")
            .document(user.userId)
            .set(userToSave, SetOptions.merge())
            .await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() {
        auth.signOut()
    }
}
