package com.cms.canteenapp.data.models

data class StockItem(
    val itemId: String = "",
    val name: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val minThreshold: Int = 10,
    val lastUpdated: Long = System.currentTimeMillis(),
    val updatedBy: String = ""
)