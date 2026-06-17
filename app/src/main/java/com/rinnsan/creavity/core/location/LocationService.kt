package com.rinnsan.creavity.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class ShippingInfo(
    val addressText: String,
    val district: String,
    val city: String,
    val countryCode: String,
    val shippingFee: Long,
    val latitude: Double,
    val longitude: Double
)

class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    // Tọa độ shop (Đà Nẵng)
    private val SHOP_LAT = 16.0544
    private val SHOP_LNG = 108.2022

    @SuppressLint("MissingPermission")
    suspend fun getCurrentShippingInfo(): ShippingInfo? = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val shippingInfo = calculateShippingFee(address, location.latitude, location.longitude)
                            continuation.resume(shippingInfo)
                        } else {
                            continuation.resume(null)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }
    }

    private fun calculateShippingFee(address: Address, lat: Double, lng: Double): ShippingInfo {
        val city = address.adminArea ?: address.locality ?: ""
        val district = address.subAdminArea ?: address.subLocality ?: ""
        val countryCode = address.countryCode ?: "VN"
        val fullAddressText = address.getAddressLine(0) ?: "$city, $countryCode"

        var fee = 0L

        if (countryCode != "VN") {
            // Quốc tế: Tạm tính đồng giá 100k, sau này có thể chia theo Châu lục
            fee = 100_000L
        } else {
            // Trong nước (VN)
            if (city.contains("Đà Nẵng", ignoreCase = true) || city.contains("Da Nang", ignoreCase = true)) {
                // Nội thành Đà Nẵng
                val distanceKm = calculateDistanceKm(SHOP_LAT, SHOP_LNG, lat, lng)
                fee = if (distanceKm <= 5.0) {
                    10_000L
                } else {
                    15_000L
                }
            } else {
                // Ngoại thành / Tỉnh khác
                fee = 30_000L
            }
        }

        return ShippingInfo(
            addressText = fullAddressText,
            district = district,
            city = city,
            countryCode = countryCode,
            shippingFee = fee,
            latitude = lat,
            longitude = lng
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Bán kính trái đất (km)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val distance = acos(sin(lat1Rad) * sin(lat2Rad) + cos(lat1Rad) * cos(lat2Rad) * cos(deltaLon)) * r
        return if (distance.isNaN()) 0.0 else distance
    }
}
