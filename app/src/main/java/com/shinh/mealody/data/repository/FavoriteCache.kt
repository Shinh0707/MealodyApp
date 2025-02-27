package com.shinh.mealody.data.repository

import android.util.Log
import com.shinh.mealody.data.model.Shop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteCache @Inject constructor(
    private val repository: MealodyRepository
) {
    // お気に入りキャッシュ
    private val _favoriteMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val favoriteMap: StateFlow<Map<String, Int>> = _favoriteMap.asStateFlow()

    private val cacheScope = CoroutineScope(Dispatchers.IO)

    init {
        preloadCache()
    }

    private fun preloadCache() {
        cacheScope.launch {
            repository.getFavoriteShops().collect { shops ->
                val newCache = shops.associate { shop ->
                    shop.id to shop.favLevel.toInt()
                }
                _favoriteMap.update { currentMap ->
                    currentMap + newCache
                }
            }
        }
    }

    // 特定のショップのお気に入りレベルを取得
    fun getFavoriteLevel(shopId: String): Int {
        return _favoriteMap.value[shopId] ?: 0
    }

    // 特定のショップのお気に入りレベルを取得
    fun getFavoriteLevel(shop: Shop): Int {
        return getFavoriteLevel(shop.id)
    }

    // お気に入りレベルを更新
    fun updateFavoriteLevel(shopId: String, level: Int) {
        _favoriteMap.update { currentMap ->
            currentMap + (shopId to level)
        }

        cacheScope.launch {
            try {
                repository.updateFavoriteLevel(shopId, level.toByte())
            } catch (e: Exception) {
                Log.e("updateFavoriteLevel", e.message ?: "")
            }
        }
    }
}