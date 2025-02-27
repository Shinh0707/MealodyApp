package com.shinh.mealody.data.repository

import com.shinh.mealody.data.database.dao.NoteDao
import com.shinh.mealody.data.database.dao.ShopDao
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.database.entity.ShopEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealodyRepository @Inject constructor(
    private val shopDao: ShopDao,
    private val noteDao: NoteDao
) {
    companion object {
        const val FAVORITES_NOTE_ID = 1
        const val FAVORITES_NOTE_NAME = "お気に入り"
    }

    suspend fun initializeFavoritesNote() {
        if (noteDao.getNoteByIdSync(FAVORITES_NOTE_ID) == null) {
            val favoritesNote = NoteEntity(
                id = FAVORITES_NOTE_ID,
                name = FAVORITES_NOTE_NAME
            )
            noteDao.insertNote(favoritesNote)
        }
    }
    suspend fun saveShop(shop: ShopEntity) {
        shopDao.insertShop(shop)
    }

    suspend fun saveShops(shops: List<ShopEntity>) {
        shopDao.insertShops(shops)
    }

    suspend fun getShopById(id: String): ShopEntity? {
        return shopDao.getShopById(id)
    }

    fun isNoteDeletable(noteId: Int): Boolean {
        return noteId != FAVORITES_NOTE_ID
    }

    suspend fun updateFavoriteLevel(shopId: String, level: Byte) {

        val validLevel = level.coerceIn(0, 3)
        shopDao.updateFavoriteLevel(shopId, validLevel)

        if (validLevel > 0) {
            addShopToNote(FAVORITES_NOTE_ID, shopId)
        } else {
            removeShopFromNote(FAVORITES_NOTE_ID, shopId)
        }
    }

    fun getFavoriteShops(): Flow<List<ShopEntity>> {
        return shopDao.getFavoriteShops()
    }

    suspend fun getFavoriteLevel(shopId: String): Int {
        return shopDao.getFavoriteLevelById(shopId)?.toInt() ?: 0
    }

    suspend fun createNote(name: String): Int {
        val note = NoteEntity(name = name)
        return noteDao.insertNote(note).toInt()
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    fun getAllNotes(): Flow<List<NoteEntity>> {
        return noteDao.getAllNotes()
    }

    fun getNoteById(id: Int): Flow<NoteEntity?> {
        return noteDao.getNoteById(id)
    }

    suspend fun addShopToNote(noteId: Int, shopId: String) {
        noteDao.addShopToNote(noteId, shopId)
    }

    suspend fun removeShopFromNote(noteId: Int, shopId: String) {
        noteDao.removeShopFromNote(noteId, shopId)
    }

    fun getShopsForNote(noteId: Int): Flow<List<ShopEntity>> {
        return noteDao.getNoteById(noteId).map { note ->
            val shopIds = note?.shops?.map { it.shopId } ?: emptyList()
            if (shopIds.isEmpty()) {
                emptyList()
            } else {
                shopDao.getShopsByIds(shopIds)
            }
        }
    }

    suspend fun getOrderedShopsForNote(noteId: Int): List<ShopEntity> {
        val note = noteDao.getNoteByIdSync(noteId) ?: return emptyList()
        val shopIds = note.shops.map { it.shopId }
        if (shopIds.isEmpty()) return emptyList()

        val shops = shopDao.getShopsByIds(shopIds)

        return shops.sortedBy { shop ->
            note.shops.find { it.shopId == shop.id }?.order ?: Byte.MAX_VALUE
        }
    }
}