package com.syme.data.session

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getUserId(): String? = firebaseAuth.currentUser?.uid

    fun logout() {
        firebaseAuth.signOut()
    }
}
