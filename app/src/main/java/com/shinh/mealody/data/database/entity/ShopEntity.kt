package com.shinh.mealody.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val genreCode: String,
    val smallImageUrl: String,
    val favLevel: Byte = 0 // 0～3のハートレベル
)