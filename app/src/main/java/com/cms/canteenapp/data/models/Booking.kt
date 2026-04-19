package com.cms.canteenapp.data.models

import java.util.UUID

data class Booking(
    val bookingId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val seatNumber: String = "",
    val bookingDate: Long = System.currentTimeMillis(),
    val mealType: MealType = MealType.LUNCH,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val qrCode: String = "",
    val checkInTime: Long? = null,
    val checkOutTime: Long? = null
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER
}

enum class BookingStatus {
    PENDING, CONFIRMED, CHECKED_IN, COMPLETED, CANCELLED
}