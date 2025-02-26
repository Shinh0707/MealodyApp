package com.shinh.mealody.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shinh.mealody.data.database.converter.Converters
import com.shinh.mealody.data.database.dao.NoteDao
import com.shinh.mealody.data.database.dao.ShopDao
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.database.entity.ShopEntity

@Database(
    entities = [ShopEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MealodyDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: MealodyDatabase? = null

        fun getDatabase(context: Context): MealodyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealodyDatabase::class.java,
                    "mealody_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}