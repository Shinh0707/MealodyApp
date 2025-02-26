package com.shinh.mealody.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationState @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    fun updateLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun updateCurrentLocation(
        checkPermission: Boolean = true,
        onPermissionRequired: () -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        if (checkPermission && !hasLocationPermission()) {
            onPermissionRequired()
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        updateLocation(LatLng(location.latitude, location.longitude))
                        onSuccess()
                    } else {
                        // 位置情報が取得できない場合
                        onError()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("LocationState", "Failed to get location", exception)
                    onError()
                }
        } catch (e: SecurityException) {
            Log.e("LocationState", "Security exception when getting location", e)
            onError()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun clearLocation() {
        _currentLocation.value = null
    }
}