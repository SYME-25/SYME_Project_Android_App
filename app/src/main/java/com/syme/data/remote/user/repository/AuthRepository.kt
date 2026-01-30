package com.syme.data.remote.user.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.syme.data.remote.user.model.UserFirebase
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(
        email: String,
        password: String,
        user: UserFirebase
    ) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("UID null")

        val userToSave = user.copy(userId = uid)

        firestore.collection("users")
            .document(uid)
            .set(userToSave)
            .await()
    }
}
