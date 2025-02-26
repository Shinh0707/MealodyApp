package com.shinh.mealody.data.database.model

import java.time.LocalDateTime

data class DateTimeInfo(
    val yearsSince2025: Byte = 0, // 2025年からの経過年（0-127）
    val month: Byte = 1,          // 1-12
    val day: Byte = 1,            // 1-31
    val hour: Byte = 0,           // 0-23
    val minute: Byte = 0          // 0-59
) {
    companion object {
        private const val BASE_YEAR = 2025

        fun now(): DateTimeInfo {
            val now = LocalDateTime.now()
            return DateTimeInfo(
                yearsSince2025 = (now.year - BASE_YEAR).toByte().coerceAtLeast(0),
                month = now.monthValue.toByte(),
                day = now.dayOfMonth.toByte(),
                hour = now.hour.toByte(),
                minute = now.minute.toByte()
            )
        }
    }

    fun toDisplayString(): String {
        val year = BASE_YEAR + yearsSince2025
        return "$year/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')} " +
                "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}