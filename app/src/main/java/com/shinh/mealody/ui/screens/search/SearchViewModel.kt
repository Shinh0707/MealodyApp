// SearchViewModel.kt
package com.shinh.mealody.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.api.HotpepperClient
import com.shinh.mealody.data.model.LargeArea
import com.shinh.mealody.data.model.MiddleArea
import com.shinh.mealody.data.model.ServiceArea
import com.shinh.mealody.data.model.SmallArea
import com.shinh.mealody.domain.model.SearchQuery
import com.shinh.mealody.ui.theme.RegionColorScheme
import com.shinh.mealody.ui.theme.RegionColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val hotpepperClient: HotpepperClient,
    private val searchManager: SearchManager
) : ViewModel() {
    private val _largeServiceAreas = MutableStateFlow<List<ServiceArea>>(emptyList())
    val largeServiceAreas: StateFlow<List<ServiceArea>> = _largeServiceAreas.asStateFlow()

    private val _largeAreas = MutableStateFlow<List<LargeArea>>(emptyList())
    val largeAreas: StateFlow<List<LargeArea>> = _largeAreas.asStateFlow()

    private val _selectedServiceArea = MutableStateFlow<ServiceArea?>(null)
    val selectedServiceArea: StateFlow<ServiceArea?> = _selectedServiceArea.asStateFlow()

    private val _selectedLargeArea = MutableStateFlow<LargeArea?>(null)
    val selectedLargeArea: StateFlow<LargeArea?> = _selectedLargeArea.asStateFlow()

    private val _selectedMiddleArea = MutableStateFlow<MiddleArea?>(null)
    val selectedMiddleArea: StateFlow<MiddleArea?> = _selectedMiddleArea.asStateFlow()

    private val _selectedSmallArea = MutableStateFlow<SmallArea?>(null)
    val selectedSmallArea: StateFlow<SmallArea?> = _selectedSmallArea.asStateFlow()

    private val _middleAreas = MutableStateFlow<List<MiddleArea>>(emptyList())
    val middleAreas: StateFlow<List<MiddleArea>> = _middleAreas.asStateFlow()

    private val _smallAreas = MutableStateFlow<List<SmallArea>>(emptyList())
    val smallAreas: StateFlow<List<SmallArea>> = _smallAreas.asStateFlow()

    // ローディング状態
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isMiddleAreasLoading = MutableStateFlow(false)
    val isMiddleAreasLoading: StateFlow<Boolean> = _isMiddleAreasLoading.asStateFlow()

    private val _isSmallAreasLoading = MutableStateFlow(false)
    val isSmallAreasLoading: StateFlow<Boolean> = _isSmallAreasLoading.asStateFlow()

    // エラー状態
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 初期化時に大エリアを読み込む
        loadLargeAreas()
    }

    private fun loadLargeAreas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                hotpepperClient.getLargeAreas()
                    .onSuccess { areas ->
                        _largeAreas.value = areas
                    }
                    .onFailure { e ->
                        _error.value = "エリア情報の取得に失敗しました: ${e.message}"
                    }
                hotpepperClient.getLargeServiceAreas()
                    .onSuccess { areas ->
                        _largeServiceAreas.value = areas
                    }
                    .onFailure { e ->
                        _error.value = "エリア情報の取得に失敗しました: ${e.message}"
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    val largeServiceAreaColorSchemes: StateFlow<Map<String, RegionColorScheme>> =
        _largeServiceAreas.map { areas ->
            areas.associate {
                it.code to RegionColors.getScheme(it.code)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    // LargeAreaのカラースキームを取得
    fun getLargeServiceAreaColorScheme(area: ServiceArea): RegionColorScheme {
        return RegionColors.getScheme(area.code)
    }

    // 選択中のServiceAreaに属するLargeArea
    val selectedAreaPrefectures: StateFlow<List<LargeArea>> =
        combine(_selectedServiceArea, _largeAreas) { selected, areas ->
            if (selected == null) emptyList()
            else areas.filter { it.parentArea.code == selected.code }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectServiceArea(area: ServiceArea) {
        _selectedServiceArea.value = area
        // ServiceAreaが変更されたら、他の選択をクリア
        _selectedLargeArea.value = null
        _selectedMiddleArea.value = null
        _selectedSmallArea.value = null
        _middleAreas.value = emptyList()
        _smallAreas.value = emptyList()
    }

    fun selectLargeArea(area: LargeArea) {
        _selectedLargeArea.value = area
        // LargeAreaが変更されたら、MiddleArea以下をクリア
        _selectedMiddleArea.value = null
        _selectedSmallArea.value = null
        _smallAreas.value = emptyList()
        // MiddleAreaを読み込む
        loadMiddleAreas(area.code)
    }

    fun selectMiddleArea(area: MiddleArea) {
        _selectedMiddleArea.value = area
        // MiddleAreaが変更されたら、SmallAreaをクリア
        _selectedSmallArea.value = null
        // SmallAreaを読み込む
        loadSmallAreas(area.code)
    }

    fun selectSmallArea(area: SmallArea) {
        _selectedSmallArea.value = area
    }

    private fun loadMiddleAreas(largeAreaCode: String) {
        viewModelScope.launch {
            _isMiddleAreasLoading.value = true
            try {
                hotpepperClient.getMiddleAreas(largeAreaCode)
                    .onSuccess { areas ->
                        _middleAreas.value = areas
                    }
                    .onFailure { e ->
                        _error.value = "市区町村情報の取得に失敗しました: ${e.message}"
                    }
            } finally {
                _isMiddleAreasLoading.value = false
            }
        }
    }

    private fun loadSmallAreas(middleAreaCode: String) {
        viewModelScope.launch {
            _isSmallAreasLoading.value = true
            try {
                hotpepperClient.getSmallAreas(middleAreaCode)
                    .onSuccess { areas ->
                        _smallAreas.value = areas
                    }
                    .onFailure { e ->
                        _error.value = "小エリア情報の取得に失敗しました: ${e.message}"
                    }
            } finally {
                _isSmallAreasLoading.value = false
            }
        }
    }
    fun performSearch(query: SearchQuery, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // 検索処理を実行（suspend関数）
                searchManager.search(query)
                // 完了時のコールバックを実行
                onComplete()
            } catch (e: Exception) {
                // エラー処理
                _error.value = "検索中にエラーが発生しました: ${e.message}"
            }
        }
    }
}