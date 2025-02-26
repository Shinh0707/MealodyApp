package com.shinh.mealody.ui.screens.restaurant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.api.HotpepperClient
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.database.entity.ShopEntity
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.repository.FavoriteCache
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val hotpepperClient: HotpepperClient,
    private val mealodyRepository: MealodyRepository,
    val favoriteCache: FavoriteCache,  // publicで公開
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shopId: String = checkNotNull(savedStateHandle["shopId"])

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val favoriteLevel = favoriteCache.favoriteMap
        .map { it[shopId]?.toByte() ?: 0 }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    init {
        loadShopDetails()
        loadNotes()
    }

    private fun loadShopDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                hotpepperClient.searchShopByID(shopId).onSuccess { fetchedShop ->
                    _shop.value = fetchedShop
                    // ショップ情報をローカルDBに保存
                    fetchedShop?.let { saveShopToDatabase(it) }
                }.onFailure { exception ->
                    _errorMessage.value = "店舗情報の取得に失敗しました: ${exception.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "エラーが発生しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveShopToDatabase(shop: Shop) {
        val shopEntity = ShopEntity(
            id = shop.id,
            name = shop.name,
            genreCode = shop.genre.code,
            smallImageUrl = shop.photo.pc.s
        )
        mealodyRepository.saveShop(shopEntity)
    }

    fun updateFavoriteLevel(level: Byte) {
        favoriteCache.updateFavoriteLevel(shopId, level.toInt())
    }

    private fun loadNotes() {
        viewModelScope.launch {
            mealodyRepository.getAllNotes()
                .collect { noteList ->
                    _notes.value = noteList
                }
        }
    }

    // 選択したノートにショップを追加
    fun addShopToNotes(noteIds: List<Int>) {
        viewModelScope.launch {
            try {
                for (noteId in noteIds) {
                    mealodyRepository.addShopToNote(noteId, shopId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "ノートへの追加に失敗しました: ${e.message}"
            }
        }
    }
}