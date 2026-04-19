package com.cms.canteenapp.data.firebase


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? = auth.currentUser

    companion object {
        const val USERS_COLLECTION = "users"
        const val BOOKINGS_COLLECTION = "bookings"
        const val SEATS_COLLECTION = "seats"
        const val SETTINGS_COLLECTION = "settings"
        const val STOCK_COLLECTION = "stock"
    }

    suspend fun createUserInFirestore(user: com.yourapp.canteen.data.models.User): Boolean {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserData(uid: String): com.yourapp.canteen.data.models.User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
            document.toObject(com.yourapp.canteen.data.models.User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}