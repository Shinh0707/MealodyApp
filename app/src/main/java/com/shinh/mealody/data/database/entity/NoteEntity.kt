package com.shinh.mealody.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shinh.mealody.data.database.model.DateTimeInfo
import com.shinh.mealody.data.database.model.ShopItem

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val modifiedTime: DateTimeInfo = DateTimeInfo.now(),
    val shops: List<ShopItem> = emptyList()
)