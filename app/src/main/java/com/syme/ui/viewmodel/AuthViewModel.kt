package com.syme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.syme.data.remote.model.UserFirebase
import com.syme.domain.mapper.toDomain
import com.syme.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
                    val querySnapshot = firestore
                        .collection("users")
                        .whereEqualTo("metadata.firebaseUid", firebaseUser.uid)
                        .limit(1)
                        .get()
                        .await()

                    val userFirebase = querySnapshot.documents
                        .firstOrNull()
                        ?.toObject(UserFirebase::class.java)

                    _currentSession.value = userFirebase?.toDomain()
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
