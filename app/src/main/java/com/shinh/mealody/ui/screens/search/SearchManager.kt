package com.shinh.mealody.ui.screens.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinh.mealody.data.api.HotpepperClient
import com.shinh.mealody.data.model.GourmetSearchResults
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.model.mergeShopLists
import com.shinh.mealody.domain.model.QueryType
import com.shinh.mealody.domain.model.SearchQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchManager @Inject constructor(
    private val hotpepperClient: HotpepperClient,
    @ApplicationContext private val context: Context
) {
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState = _searchState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Shop>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _selectedShop = MutableStateFlow<Shop?>(null)
    val selectedShop = _selectedShop.asStateFlow()

    private val _lastSearchQuery = MutableStateFlow<SearchQuery?>(null)
    val lastSearchQuery = _lastSearchQuery.asStateFlow()

    private val _lastResponse = MutableStateFlow<GourmetSearchResults?>(null)
    val lastResponse = _lastResponse.asStateFlow()

    val storedShops: StateFlow<Int> = _lastResponse.map { response ->
        (response?.resultsReturned ?: 0) + (response?.resultsStart ?: 1) - 1
    }.stateIn(
        scope = managerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val availableShops: StateFlow<Int> = _lastResponse.map { response ->
        response?.resultsAvailable ?: 0
    }.stateIn(
        scope = managerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val availableMore: StateFlow<Boolean> = _lastResponse.map { response ->
        response?.hasMoreResults() ?: false
    }.stateIn(
        scope = managerScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    suspend fun search(query: SearchQuery): Result<List<Shop>> {
        _searchState.value = SearchState.Loading
        _lastSearchQuery.value = query

        return try {
            hotpepperClient.search(query)
                .map { results ->
                    _lastResponse.value = results
                    _searchResults.value = results.shop
                    _searchState.value = if (results.shop.isEmpty()) {
                        SearchState.Empty
                    } else {
                        SearchState.Success
                    }
                    results.shop
                }
        } catch (e: Exception) {
            _searchState.value = SearchState.Error
            Result.failure(e)
        }
    }

    suspend fun searchMore(): Result<List<Shop>> {
        val lastQuery = _lastSearchQuery.value ?: return Result.failure(
            IllegalStateException("No previous query")
        )

        val lastResp = _lastResponse.value ?: return Result.failure(
            IllegalStateException("No previous response")
        )

        if (!availableMore.value) {
            return Result.success(_searchResults.value)
        }
        val query = lastQuery.nextPageQuery(lastResp)
        Log.d("SearchManager", "searchMore: ${query?.start} ${query?.count}")

        return try {
            query?.let {
                hotpepperClient.search(it)
                    .onSuccess { results ->
                        Log.d("SearchManager", "searchMore: ${results.resultsStart}")
                        _lastSearchQuery.value = it
                        _lastResponse.value = results
                        val mergedList = mergeShopLists(results.shop, _searchResults.value)
                        _searchResults.value = mergedList
                        Result.success(_searchResults.value)
                    }
                    .onFailure {

                    }
                Result.failure(Exception("No next page"))
            } ?: Result.failure(Exception("No next page"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun selectShop(shop: Shop) {
        managerScope.launch {
            try {
                hotpepperClient.searchShopByID(shop.id)
                    .onSuccess { result ->
                        _selectedShop.value = result ?: shop
                        hotpepperClient.addToHistory(result ?: shop)
                    }
                    .onFailure {
                        _selectedShop.value = shop
                    }
            } catch (e: Exception) {
                _selectedShop.value = shop
            }
        }
    }

    fun setError(){
        _searchState.value = SearchState.Error
    }

    fun setEmpty(){
        _searchState.value = SearchState.Empty
    }

    fun clearSelectedShop() {
        _selectedShop.value = null
    }

    fun resetSearch() {
        _searchState.value = SearchState.Initial
        _searchResults.value = emptyList()
        _lastSearchQuery.value = null
        _lastResponse.value = null
        _selectedShop.value = null
    }

    fun onDestroy() {
        managerScope.cancel()
    }
}

sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    object Success : SearchState()
    object Empty : SearchState()
    object Error : SearchState()
}