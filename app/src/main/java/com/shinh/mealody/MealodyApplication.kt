package com.shinh.mealody

import android.app.Application
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MealodyApplication : Application(){
    @Inject
    lateinit var repository: MealodyRepository

    override fun onCreate() {
        super.onCreate()

        // アプリ起動時にお気に入りノートを初期化
        CoroutineScope(Dispatchers.IO).launch {
            repository.initializeFavoritesNote()
        }
    }
}