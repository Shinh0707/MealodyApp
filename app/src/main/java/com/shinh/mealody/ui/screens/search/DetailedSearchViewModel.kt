package com.shinh.mealody.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.model.SmallArea
import com.shinh.mealody.data.repository.FavoriteCache
import com.shinh.mealody.data.repository.MealodyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // 特定のショップのお気に入りレベルを取得
    fun getFavLevel(shop: Shop): Int {
        return favoriteCache.getFavoriteLevel(shop)
    }

    // お気に入りレベルを更新
    fun updateHeartLevel(shopId: String, level: Int) {
        favoriteCache.updateFavoriteLevel(shopId, level)
    }

    // 検索結果を初期化（SearchManagerから結果を取得）
    fun initializeSearchResults() {
        viewModelScope.launch {
            val results = searchManager.searchResults.value
            if (results.isNotEmpty()) {
                // 最初の店舗からエリア情報を参照（もしあれば）
                _matchingArea.value = results.firstOrNull()?.getArea()
            } else {
                // 検索結果がない場合は空の状態を設定
                searchManager.setEmpty()
            }
        }
    }

    // 検索結果を更新
    fun refreshSearchResults() {
        viewModelScope.launch {
            val results = searchManager.searchResults.value
            // 結果が空の場合は空の状態を設定
            if (results.isEmpty()) {
                searchManager.setEmpty()
            }
        }
    }

    // さらに結果を読み込む
    fun searchMore() {
        viewModelScope.launch {
            searchManager.searchMore()
        }
    }
    // ノートリストを取得
    val notes = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ショップをノートに追加
    fun addShopToNotes(shopId: String, noteIds: List<Int>) {
        viewModelScope.launch {
            try {
                for (noteId in noteIds) {
                    repository.addShopToNote(noteId, shopId)
                }
            } catch (e: Exception) {
                // エラー処理
            }
        }
    }
}