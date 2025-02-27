package com.shinh.mealody.data.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shinh.mealody.data.database.model.DateTimeInfo
import com.shinh.mealody.data.database.model.ShopItem

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromShopListToString(shopList: List<ShopItem>): String {
        return gson.toJson(shopList)
    }

    @TypeConverter
    fun fromStringToShopList(value: String): List<ShopItem> {
        if (value.isBlank()) return emptyList()
        return try {
            val listType = object : TypeToken<List<ShopItem>>() {}.type
            gson.fromJson(value, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDateTimeInfoToString(dateTimeInfo: DateTimeInfo): String {
        return gson.toJson(dateTimeInfo)
    }

    @TypeConverter
    fun fromStringToDateTimeInfo(value: String): DateTimeInfo {
        return try {
            gson.fromJson(value, DateTimeInfo::class.java)
        } catch (e: Exception) {
            DateTimeInfo.now()
        }
    }
}