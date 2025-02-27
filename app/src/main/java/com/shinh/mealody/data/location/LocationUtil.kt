package com.shinh.mealody.data.location

import android.location.Address
import android.location.Geocoder
import android.content.Context
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale

class LocationUtil {
    companion object {
        suspend fun getAddress(context: Context, latLng: LatLng): Address? =
            suspendCancellableCoroutine { continuation ->
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    ) { addresses ->
                        continuation.resume(addresses.firstOrNull()) { _, _, _ -> }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val address = try {
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.firstOrNull()
                    } catch (e: Exception) {
                        null
                    }
                    continuation.resume(address) { _, _, _ -> }
                }
            }
    }
}