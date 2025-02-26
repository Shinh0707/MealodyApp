package com.shinh.mealody.di

import android.content.Context
import com.shinh.mealody.data.database.MealodyDatabase
import com.shinh.mealody.data.database.dao.NoteDao
import com.shinh.mealody.data.database.dao.ShopDao
import com.shinh.mealody.data.repository.FavoriteCache
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context): MealodyDatabase {
        return MealodyDatabase.getDatabase(appContext)
    }

    @Provides
    fun provideShopDao(database: MealodyDatabase): ShopDao {
        return database.shopDao()
    }

    @Provides
    fun provideNoteDao(database: MealodyDatabase): NoteDao {
        return database.noteDao()
    }
    @Provides
    @Singleton
    fun provideFavoriteCache(repository: MealodyRepository): FavoriteCache {
        return FavoriteCache(repository)
    }
}