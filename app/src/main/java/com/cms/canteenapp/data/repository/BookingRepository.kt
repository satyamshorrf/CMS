package com.cms.canteenapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.yourapp.canteen.data.firebase.FirebaseManager
import com.yourapp.canteen.data.models.Booking
import com.yourapp.canteen.data.models.Seat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val firebaseManager: FirebaseManager
) {
    private val firestore = firebaseManager.firestore

    suspend fun createBooking(booking: Booking): Result<Booking> {
        return try {
            // Check if seat is available
            val seatDoc = firestore.collection(FirebaseManager.SEATS_COLLECTION)
                .document(booking.seatNumber)
                .get()
                .await()

            val seat = seatDoc.toObject(Seat::class.java)
            if (seat?.isAvailable != true) {
                return Result.failure(Exception("Seat is not available"))
            }

            // Create booking
            firestore.collection(FirebaseManager.BOOKINGS_COLLECTION)
                .document(booking.bookingId)
                .set(booking)
                .await()

            // Update seat status
            firestore.collection(FirebaseManager.SEATS_COLLECTION)
                .document(booking.seatNumber)
                .update(
                    mapOf(
                        "isAvailable" to false,
                        "currentBookingId" to booking.bookingId
                    )
                )
                .await()

            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserBookings(userId: String): Flow<List<Booking>> = flow {
        try {
            val snapshot = firestore.collection(FirebaseManager.BOOKINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("bookingDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val bookings = value?.documents?.mapNotNull {
                        it.toObject(Booking::class.java)
                    } ?: emptyList()
                    try { emit(bookings) } catch (e: Exception) {}
                }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            val doc = firestore.collection(FirebaseManager.BOOKINGS_COLLECTION)
                .document(bookingId)
                .get()
                .await()
            doc.toObject(Booking::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            // Get booking to free up seat
            val booking = getBookingById(bookingId)

            // Update booking status
            firestore.collection(FirebaseManager.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update("status", "CANCELLED")
                .await()

            // Free up seat
            if (booking != null) {
                firestore.collection(FirebaseManager.SEATS_COLLECTION)
                    .document(booking.seatNumber)
                    .update(
                        mapOf(
                            "isAvailable" to true,
                            "currentBookingId" to null
                        )
                    )
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIn(bookingId: String): Result<Unit> {
        return try {
            firestore.collection(FirebaseManager.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(
                    mapOf(
                        "status" to "CHECKED_IN",
                        "checkInTime" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAvailableSeats(): Flow<List<Seat>> = flow {
        try {
            val snapshot = firestore.collection(FirebaseManager.SEATS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val seats = value?.documents?.mapNotNull {
                        it.toObject(Seat::class.java)
                    } ?: emptyList()
                    try { emit(seats) } catch (e: Exception) {}
                }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}