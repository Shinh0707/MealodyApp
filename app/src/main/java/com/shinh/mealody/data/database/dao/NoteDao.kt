package com.shinh.mealody.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.database.model.DateTimeInfo
import com.shinh.mealody.data.database.model.ShopItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY modifiedTime DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: Int): NoteEntity?

    @Transaction
    suspend fun addShopToNote(noteId: Int, shopId: String) {
        val note = getNoteByIdSync(noteId) ?: return
        val shops = note.shops.toMutableList()

        // すでに同じshopIdが存在する場合は追加しない
        if (shops.none { it.shopId == shopId }) {
            // 追加するアイテムの順序は現在のリストの最大順序+1
            val maxOrder = shops.maxOfOrNull { it.order }?.toInt() ?: -1
            shops.add(ShopItem(shopId, (maxOrder + 1).toByte()))

            updateNote(note.copy(
                shops = shops,
                modifiedTime = DateTimeInfo.now()
            ))
        }
    }

    @Transaction
    suspend fun removeShopFromNote(noteId: Int, shopId: String) {
        val note = getNoteByIdSync(noteId) ?: return
        val shops = note.shops.toMutableList()

        // 指定されたshopIdを削除
        if (shops.removeIf { it.shopId == shopId }) {
            // 並び順を再整理
            val reorderedShops = shops.mapIndexed { index, item ->
                item.copy(order = index.toByte())
            }

            updateNote(note.copy(
                shops = reorderedShops,
                modifiedTime = DateTimeInfo.now()
            ))
        }
    }
}