package com.quantumslate.dashboard.data.repository

import com.quantumslate.dashboard.data.local.DeviceLocationProvider
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.domain.model.Weather
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single place that decides *where* weather is fetched for.
 *
 * Previously four call sites each decided independently, and three of them were wrong:
 * `DashboardUpdateWorker` read a latitude preference nothing ever wrote and fell back to
 * New York, `RealtimeSyncService` passed `0.0, 0.0` (the Gulf of Guinea), and
 * `WeatherViewModel` hardcoded New York outright. Because every path writes the same cached
 * row, a background sync would silently replace the user's local weather with New York's.
 *
 * Resolution order, applied identically everywhere:
 *  1. device location, when the user opted in and granted permission
 *  2. the city typed in Settings
 *  3. an explicit failure naming what to do — never a silent default
 */
@Singleton
class WeatherLocationResolver @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val deviceLocationProvider: DeviceLocationProvider,
    private val weatherRepository: WeatherRepository
) {
    suspend fun fetchForCurrentLocation(): Result<Weather> {
        val useDeviceLocation = preferencesManager.getLocationEnabled()
        val fix = if (useDeviceLocation) deviceLocationProvider.lastKnownLocation() else null
        val typed = preferencesManager.getLocation()

        return when {
            fix != null -> weatherRepository.fetchAndCacheWeather(fix.latitude, fix.longitude)
            !typed.isNullOrEmpty() -> weatherRepository.fetchWeatherByLocationName(typed)
            useDeviceLocation && !deviceLocationProvider.hasPermission() ->
                Result.failure(Exception("Location permission needed, or set a city in Settings."))
            useDeviceLocation ->
                Result.failure(Exception("No recent location fix. Set a city in Settings."))
            else -> Result.failure(Exception("Set a city in Settings to see weather."))
        }
    }

    fun isBlockedOnPermission(): Boolean =
        preferencesManager.getLocationEnabled() && !deviceLocationProvider.hasPermission()
}
