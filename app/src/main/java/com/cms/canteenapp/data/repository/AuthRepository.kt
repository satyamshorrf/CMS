package com.cms.canteenapp.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.yourapp.canteen.data.firebase.FirebaseManager
import com.yourapp.canteen.data.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseManager: FirebaseManager
) {
    private val auth = firebaseManager.auth

    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(firebaseUser.toUser())
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun sendVerificationCode(
        phoneNumber: String,
        activity: android.app.Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<User> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Check if user exists in Firestore
                var user = firebaseManager.getUserData(firebaseUser.uid)
                if (user == null) {
                    // Create new user
                    user = User(
                        uid = firebaseUser.uid,
                        phoneNumber = firebaseUser.phoneNumber ?: "",
                        name = firebaseUser.displayName ?: "",
                        isAdmin = false
                    )
                    firebaseManager.createUserInFirestore(user)
                }
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            phoneNumber = phoneNumber ?: "",
            name = displayName ?: "",
            email = email ?: "",
            isAdmin = false // This should be fetched from Firestore
        )
    }
}