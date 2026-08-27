package com.quantumslate.dashboard.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Last known device location, for weather.
 *
 * Uses the platform [LocationManager] rather than Play Services' fused provider: this app
 * has no other Google Play dependency, and a weather lookup does not need metre accuracy or
 * a live fix. Reading the last known position also avoids powering up the GPS radio, which
 * matters for a dashboard meant to sit on a desk all day.
 */
@Singleton
class DeviceLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager: LocationManager? =
        ContextCompat.getSystemService(context, LocationManager::class.java)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * @return the most recent usable fix, or null if permission is missing, location is off,
     *         or no provider has reported a position recently enough to trust.
     */
    suspend fun lastKnownLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext null
        val manager = locationManager ?: return@withContext null

        val cutoff = System.currentTimeMillis() - MAX_FIX_AGE_MS

        return@withContext try {
            // Take the newest fix across providers; NETWORK is usually freshest indoors,
            // which is where a desk dashboard lives.
            manager.getProviders(true)
                .mapNotNull { provider ->
                    @Suppress("MissingPermission")
                    manager.getLastKnownLocation(provider)
                }
                .filter { it.time >= cutoff }
                .maxByOrNull { it.time }
        } catch (e: SecurityException) {
            null
        }
    }

    private companion object {
        /** Older than this and the user has probably moved; fall back to the typed city. */
        val MAX_FIX_AGE_MS: Long = TimeUnit.HOURS.toMillis(6)
    }
}
