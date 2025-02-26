package com.shinh.mealody.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shinh.mealody.data.database.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShops(shops: List<ShopEntity>)

    @Query("SELECT * FROM shops WHERE id = :id")
    suspend fun getShopById(id: String): ShopEntity?

    @Query("SELECT * FROM shops WHERE favLevel > 0 ORDER BY favLevel DESC")
    fun getFavoriteShops(): Flow<List<ShopEntity>>

    @Query("UPDATE shops SET favLevel = :level WHERE id = :shopId")
    suspend fun updateFavoriteLevel(shopId: String, level: Byte)

    @Query("SELECT * FROM shops WHERE id IN (:shopIds)")
    suspend fun getShopsByIds(shopIds: List<String>): List<ShopEntity>

    @Query("SELECT * FROM shops WHERE id IN (:shopIds)")
    fun getShopsByIdsFlow(shopIds: List<String>): Flow<List<ShopEntity>>

    @Query("SELECT favLevel FROM shops WHERE id = :shopId")
    suspend fun getFavoriteLevelById(shopId: String): Byte?
}