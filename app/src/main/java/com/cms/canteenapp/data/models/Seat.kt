package com.cms.canteenapp.data.models


data class Seat(
    val seatNumber: String = "",
    val isAvailable: Boolean = true,
    val currentBookingId: String? = null,
    val capacity: Int = 1
)