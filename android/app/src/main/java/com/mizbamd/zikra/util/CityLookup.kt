package com.mizbamd.zikra.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val CacheRadiusKm = 5.0
private const val GeocodeTimeoutMs = 5_000L

/** City label for the calendar header. Not prayer times or Qibla. */
object CityLookup {
    fun shouldRefresh(
        lat: Double,
        lon: Double,
        cachedLat: Double?,
        cachedLon: Double?,
        cachedCity: String?,
    ): Boolean {
        if (cachedCity.isNullOrBlank() || cachedLat == null || cachedLon == null) return true
        return kmBetween(lat, lon, cachedLat, cachedLon) >= CacheRadiusKm
    }

    suspend fun lookup(context: Context, lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return withTimeoutOrNull(GeocodeTimeoutMs) {
            runCatching {
                if (Build.VERSION.SDK_INT >= 33) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lon, 1) { addresses ->
                            if (cont.isActive) cont.resume(cityFrom(addresses))
                        }
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        @Suppress("DEPRECATION")
                        cityFrom(geocoder.getFromLocation(lat, lon, 1))
                    }
                }
            }.getOrNull()
        }
    }

    private fun cityFrom(addresses: List<Address>?): String? {
        val a = addresses?.firstOrNull() ?: return null
        return a.locality?.trim()?.takeIf { it.isNotEmpty() }
            ?: a.subAdminArea?.trim()?.takeIf { it.isNotEmpty() }
            ?: a.adminArea?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun kmBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * earthKm * asin(min(1.0, sqrt(h)))
    }
}
