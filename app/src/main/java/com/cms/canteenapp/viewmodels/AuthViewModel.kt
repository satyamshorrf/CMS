package com.cms.canteenapp.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.yourapp.canteen.data.models.User
import com.yourapp.canteen.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    _isLoading.value = false
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(exception: Exception) {
                    _isLoading.value = false
                    _error.value = exception.message
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    _isLoading.value = false
                    _verificationId.value = verificationId
                    _authState.value = AuthState.CodeSent(phoneNumber)
                }
            }

            authRepository.sendVerificationCode(phoneNumber, activity, callbacks)
        }
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val verificationId = _verificationId.value
            if (verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithCredential(credential)
            } else {
                _isLoading.value = false
                _error.value = "Verification ID not found"
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun clearError() {
        _error.value = null
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class CodeSent(val phoneNumber: String) : AuthState()
    data class Authenticated(val user: User) : AuthState()
}