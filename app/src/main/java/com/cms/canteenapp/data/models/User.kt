package com.cms.canteenapp.data.models

data class User(
    val uid: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val employeeId: String = "",
    val department: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)