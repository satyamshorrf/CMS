package com.cms.canteenapp.data.models

data class CanteenSettings(
    val totalSeats: Int = 100,
    val breakfastTiming: String = "8:00 AM - 10:00 AM",
    val lunchTiming: String = "12:00 PM - 2:00 PM",
    val dinnerTiming: String = "7:00 PM - 9:00 PM",
    val maxBookingPerDay: Int = 1,
    val allowAdvanceBooking: Boolean = true,
    val advanceBookingDays: Int = 3
)