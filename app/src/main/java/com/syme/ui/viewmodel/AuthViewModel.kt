package com.syme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.syme.data.repository.UserRepository
import com.syme.domain.mapper.toDomain
import com.syme.domain.model.User
import com.syme.domain.state.AuthState
import com.syme.service.FcmTokenHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser

        Log.d("AUTH_DEBUG", "Firebase user: ${firebaseUser?.uid}")

        if (firebaseUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Loading

            viewModelScope.launch {
                loadUserSession(firebaseUser.uid)
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private suspend fun loadUserSession(uid: String) {
        try {
            val querySnapshot = firestore
                .collection("users")
                .whereEqualTo("metadata.firebaseUid", uid)
                .limit(1)
                .get()
                .await()

            val userFirebase = querySnapshot.documents
                .firstOrNull()
                ?.toObject(User::class.java)

            if (userFirebase == null) {
                Log.w("AuthViewModel", "Utilisateur Firestore introuvable pour uid=$uid")
                _authState.value = AuthState.Unauthenticated
                return
            }

            val domainUser = userFirebase.toDomain()

            _authState.value = AuthState.Authenticated(domainUser)

            Log.d("AuthViewModel", "Session chargée pour userId=${domainUser.userId}")

            refreshFcmToken(domainUser.userId)

        } catch (e: Exception) {
            Log.e("AuthViewModel", "Erreur chargement session Firestore", e)

            // Ici on considère que la session n'est pas exploitable
            _authState.value = AuthState.Unauthenticated
        }
    }

    private suspend fun refreshFcmToken(userId: String) {
        try {
            val freshToken = FirebaseMessaging.getInstance().token.await()

            Log.d("FCM_TOKEN", "Token session: $freshToken")

            FcmTokenHolder.token = freshToken
            userRepository.addFcmToken(userId, freshToken)

        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Échec récupération token FCM", e)

            val cachedToken = FcmTokenHolder.token

            if (cachedToken != null) {
                try {
                    userRepository.addFcmToken(userId, cachedToken)
                } catch (e2: Exception) {
                    Log.e("FCM_TOKEN", "Échec sauvegarde token en cache", e2)
                }
            }
        }
    }

    fun logout() {
        val token = FcmTokenHolder.token

        _authState.value = AuthState.Unauthenticated

        val firebaseUser = auth.currentUser

        if (firebaseUser != null && token != null) {
            viewModelScope.launch {
                try {
                    userRepository.removeFcmToken(firebaseUser.uid, token)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Échec suppression token FCM", e)
                }
            }
        }

        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}