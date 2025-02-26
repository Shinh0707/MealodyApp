package com.shinh.mealody.data.database.model

data class ShopItem(
    val shopId: String,
    val order: Byte = 0  // 最大255件
)