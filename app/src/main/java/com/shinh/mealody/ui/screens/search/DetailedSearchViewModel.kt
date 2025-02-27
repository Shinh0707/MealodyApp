package com.shinh.mealody.ui.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.database.entity.ShopEntity
import com.shinh.mealody.data.model.Area
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
class DetailedSearchViewModel @Inject constructor(
    internal val searchManager: SearchManager,
    private val repository: MealodyRepository,
    private val favoriteCache: FavoriteCache
) : ViewModel() {
    private val _matchingArea = MutableStateFlow<Area?>(null)
    val matchingArea: StateFlow<Area?> = _matchingArea.asStateFlow()

    fun getFavLevel(shop: Shop): Int {
        return favoriteCache.getFavoriteLevel(shop)
    }

    fun initializeSearchResults() {
        viewModelScope.launch {
            val results = searchManager.searchResults.value
            if (results.isNotEmpty()) {
                _matchingArea.value = results.firstOrNull()?.getArea()
            } else {
                searchManager.setEmpty()
            }
        }
    }

    fun refreshSearchResults() {
        viewModelScope.launch {
            val results = searchManager.searchResults.value
            if (results.isEmpty()) {
                searchManager.setEmpty()
            }
        }
    }

    fun searchMore() {
        viewModelScope.launch {
            searchManager.searchMore()
        }
    }

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    init {
        loadNotes()
    }

    private suspend fun saveShopToDatabase(shop: Shop) {
        val shopEntity = ShopEntity(
            id = shop.id,
            name = shop.name,
            genreCode = shop.genre.code,
            smallImageUrl = shop.photo.pc.s
        )
        repository.saveShop(shopEntity)
    }

    fun updateFavoriteLevel(shopId: String, level: Int) {
        favoriteCache.updateFavoriteLevel(shopId, level)
    }

    private fun loadNotes() {
        viewModelScope.launch {
            repository.getAllNotes()
                .collect { noteList ->
                    _notes.value = noteList
                }
        }
    }

    fun addShopToNotes(shopId: String, noteIds: List<Int>) {
        viewModelScope.launch {
            try {
                for (noteId in noteIds) {
                    repository.addShopToNote(noteId, shopId)
                }
            } catch (e: Exception) {
                Log.e("AddShopToNotes", e.message ?: "")
            }
        }
    }

    fun selectShop(shop: Shop){
        viewModelScope.launch {
            try {
                saveShopToDatabase(shop)
            } catch (e: Exception){
                Log.e("Select Shop", e.message ?: "")
            }
        }
        searchManager.selectShop(shop)
    }
}