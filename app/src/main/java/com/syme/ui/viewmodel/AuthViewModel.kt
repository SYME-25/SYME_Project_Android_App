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

    private val _currentSession = MutableStateFlow<User?>(null)
    val currentSession: StateFlow<User?> = _currentSession

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser == null) {
                _currentSession.value = null
            } else {
                viewModelScope.launch {
                    // 1️⃣ Récupérer l'utilisateur Firestore
                    val querySnapshot = firestore
                        .collection("users")
                        .whereEqualTo("metadata.firebaseUid", firebaseUser.uid)
                        .limit(1)
                        .get()
                        .await()

                    val userFirebase = querySnapshot.documents
                        .firstOrNull()
                        ?.toObject(User::class.java)

                    _currentSession.value = userFirebase?.toDomain()

                    if (userFirebase == null) {
                        Log.w("AuthViewModel", "Utilisateur Firestore introuvable pour uid=${firebaseUser.uid}")
                        return@launch
                    }

                    // 2️⃣ Forcer la récupération du token FCM frais à chaque session
                    try {
                        val freshToken = FirebaseMessaging.getInstance().token.await()
                        Log.d("FCM_TOKEN", "Token session: $freshToken")

                        // Mettre à jour le holder global
                        FcmTokenHolder.token = freshToken

                        // Sauvegarder dans Firestore
                        userRepository.addFcmToken(userFirebase.userId, freshToken)

                    } catch (e: Exception) {
                        Log.e("FCM_TOKEN", "Échec récupération token FCM", e)

                        // Fallback : utiliser le token en cache si disponible
                        val cachedToken = FcmTokenHolder.token
                        if (cachedToken != null) {
                            Log.d("FCM_TOKEN", "Utilisation du token en cache: $cachedToken")
                            userRepository.addFcmToken(userFirebase.userId, cachedToken)
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        val user = _currentSession.value
        val token = FcmTokenHolder.token

        if (user != null && token != null) {
            viewModelScope.launch {
                try {
                    userRepository.removeFcmToken(user.userId, token)
                    Log.d("AuthViewModel", "Token FCM supprimé pour userId=${user.userId}")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Échec suppression token FCM", e)
                }
            }
        }

        auth.signOut()
    }
}