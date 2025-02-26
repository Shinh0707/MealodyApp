package com.shinh.mealody.ui.screens.home

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.shinh.mealody.data.api.HotpepperClient
import com.shinh.mealody.data.location.LocationState
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.model.SmallArea
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationState: LocationState,
    private val hotpepperClient: HotpepperClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _matchingArea = MutableStateFlow<SmallArea?>(null)
    val matchingArea: StateFlow<SmallArea?> = _matchingArea.asStateFlow()

    init {
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        locationState.updateCurrentLocation(
            checkPermission = true,
            onPermissionRequired = {
                _hasLocationPermission.value = false
            },
            onSuccess = {
                _hasLocationPermission.value = true
                showCurrentAddress()
            },
            onError = {
                Toast.makeText(context, "位置情報の取得に失敗しました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun onLocationPermissionGranted() {
        _hasLocationPermission.value = true
        locationState.updateCurrentLocation(
            checkPermission = false,
            onSuccess = {
                showCurrentAddress()
            },
            onError = {
                Toast.makeText(context, "位置情報の取得に失敗しました", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun onLocationPermissionDenied() {
        _hasLocationPermission.value = false
        Toast.makeText(context, "位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
    }

    private fun showCurrentAddress() {
        locationState.currentLocation.value?.let { latLng ->
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                ) { addresses ->
                    addresses.firstOrNull()?.let { address ->
                        viewModelScope.launch {
                            _matchingArea.value = hotpepperClient.findMatchingArea(address)
                        }
                    }
                }
            } else {
                viewModelScope.launch {
                    try {
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        addresses?.firstOrNull()?.let { address ->
                            _matchingArea.value = hotpepperClient.findMatchingArea(address)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "住所の取得に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun onSearchByCurrentLocation(
        onSearchByCurrentLocation: (LatLng) -> Unit
    ) {
        locationState.currentLocation.value?.let { latLng ->
            // TODO: ここで現在地を使った検索を実行
            // Navigatorを使って検索結果画面に遷移
            onSearchByCurrentLocation(latLng)
        }
    }

    // 画面が破棄されるときにクリーンアップ
    override fun onCleared() {
        super.onCleared()
        locationState.clearLocation()
    }

    // 履歴データを公開
    val visitedShops: StateFlow<List<Shop>> = hotpepperClient.visitedShops

    // 店舗詳細へ遷移する際の処理
    fun onShopSelected(shop: Shop, onNavigateToDetail: (Shop) -> Unit) {
        // 必要に応じて追加の処理
        onNavigateToDetail(shop)
    }

}