package com.shinh.mealody.ui.screens.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.shinh.mealody.data.api.HotpepperClient
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.database.entity.ShopEntity
import com.shinh.mealody.data.location.LocationUtil
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.model.SmallArea
import com.shinh.mealody.data.repository.FavoriteCache
import com.shinh.mealody.data.repository.MealodyRepository
import com.shinh.mealody.domain.model.QueryType
import com.shinh.mealody.domain.model.SearchQuery
import com.shinh.mealody.math.calculateDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NearbySearchViewModel @Inject constructor(
    private val hotpepperClient: HotpepperClient,
    internal val searchManager: SearchManager,
    private val repository: MealodyRepository,
    private val favoriteCache: FavoriteCache,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _currentRange = MutableStateFlow(1000f)
    val currentRange = _currentRange.asStateFlow()

    private val _matchingArea = MutableStateFlow<SmallArea?>(null)
    val matchingArea = _matchingArea.asStateFlow()

    private val _nearbyShops = MutableStateFlow<List<Shop>>(emptyList())
    val nearbyShops = _nearbyShops.asStateFlow()

    private var currentLocation: LatLng? = null

    fun initialize(latLng: LatLng) {
        searchManager.resetSearch()
        viewModelScope.launch {
            try {
                currentLocation = latLng

                val address = LocationUtil.getAddress(context, latLng)
                address?.let { addr ->
                    Log.d("NearbySearch", """
                    Address found:
                    - Admin Area: ${addr.adminArea}
                    - Locality: ${addr.locality}
                    - SubLocality: ${addr.subLocality}
                    - Thoroughfare: ${addr.thoroughfare}
                    Full Address: ${addr.getAddressLine(0)}
                """.trimIndent())

                    _matchingArea.value = hotpepperClient.findMatchingArea(addr)
                }

                val rangeLevel = calculateRangeLevel(_currentRange.value)
                searchNearbyShops(latLng, rangeLevel)
            } catch (e: Exception) {
                searchManager.setError()
            }
        }
    }

    fun updateRange(newRange: Float) {
        _currentRange.value = newRange
        filterShopsByDistance(newRange.roundToInt())
    }

    private suspend fun searchNearbyShops(latLng: LatLng, rangeLevel: Int) {
        val query = SearchQuery(
            lat = latLng.latitude,
            lng = latLng.longitude,
            range = rangeLevel,
            type = QueryType.LITE,
            count = 100
        )

        searchManager.search(query)
            .onSuccess { _ ->
                filterShopsByDistance(_currentRange.value.roundToInt())
            }
    }

    fun searchMore() {
        viewModelScope.launch {
            searchManager.searchMore().onSuccess { _ ->
                filterShopsByDistance(_currentRange.value.roundToInt())
            }
        }
    }

    private fun filterShopsByDistance(rangeMeters: Int) {
        currentLocation?.let { location ->
            val filteredShops = searchManager.searchResults.value.filter { shop ->
                calculateDistance(
                    location.latitude, location.longitude,
                    shop.lat, shop.lng
                ) <= rangeMeters
            }

            _nearbyShops.value = filteredShops
            if (filteredShops.isEmpty()) {
                searchManager.setEmpty()
            }
        }
    }

    private fun calculateRangeLevel(rangeMeters: Float): Int {
        return when {
            rangeMeters <= 300 -> 1
            rangeMeters <= 500 -> 2
            rangeMeters <= 1000 -> 3
            rangeMeters <= 2000 -> 4
            else -> 5
        }
    }
    fun getFavLevel(shop: Shop): Int {
        return favoriteCache.getFavoriteLevel(shop)
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