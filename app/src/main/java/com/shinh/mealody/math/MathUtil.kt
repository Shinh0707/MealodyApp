package com.shinh.mealody.math

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MathUtil {

}
fun estimateWalkingMinutes(latLng1: LatLng, latLng2: LatLng, walkingSpeedMetersPerMinute: Double=50.0): Int {
    val distanceMeters = calculateDistance(
        latLng1, latLng2
    )
    return ceil(distanceMeters / walkingSpeedMetersPerMinute).toInt()
}
fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
    return calculateDistance(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude)
}
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3
    val p1 = lat1 * PI / 180
    val p2 = lat2 * PI / 180
    val dp = (lat2 - lat1) * PI / 180
    val dl = (lon2 - lon1) * PI / 180

    val a = sin(dp/2) * sin(dp/2) +
            cos(p1) * cos(p2) *
            sin(dl/2) * sin(dl/2)
    val c = 2 * atan2(sqrt(a), sqrt(1-a))

    return r * c
}