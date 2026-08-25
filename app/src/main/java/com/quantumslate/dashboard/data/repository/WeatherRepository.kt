package com.quantumslate.dashboard.data.repository

import android.content.Context
import android.location.Geocoder
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.local.WeatherDao
import com.quantumslate.dashboard.data.local.WeatherEntity
import com.quantumslate.dashboard.data.remote.ApiClient
import com.quantumslate.dashboard.domain.model.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherDao: WeatherDao,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) {
    val weather: Flow<Weather?> = weatherDao.getWeather().map { it?.toDomainModel() }.flowOn(Dispatchers.IO)

    suspend fun fetchAndCacheWeather(lat: Double, lon: Double): Result<Weather> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = preferencesManager.getOpenWeatherApiKey()
                if (apiKey.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("OpenWeatherMap API key not configured"))
                }

                val apiService = ApiClient.getWeatherApiService()
                val response = apiService.getCurrentWeather(lat, lon, apiKey)
                val weather = response.toDomainModel()

                // Cache in database
                weatherDao.insertWeather(
                    WeatherEntity(
                        id = 0,
                        temperature = weather.temperature,
                        condition = weather.condition,
                        highTemp = weather.highTemp,
                        lowTemp = weather.lowTemp,
                        location = weather.location,
                        timestamp = weather.timestamp,
                        iconUrl = weather.iconUrl,
                        humidity = weather.humidity
                    )
                )

                Result.success(weather)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchWeatherByLocationName(locationName: String): Result<Weather> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(locationName, 1)

                if (addresses.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Location not found"))
                }

                val address = addresses.first()
                return@withContext fetchAndCacheWeather(address.latitude, address.longitude)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCachedWeather(): Weather? {
        return withContext(Dispatchers.IO) {
            weatherDao.getWeatherOnce()?.toDomainModel()
        }
    }

    fun WeatherEntity.toDomainModel(): Weather {
        return Weather(
            temperature = temperature,
            condition = condition,
            highTemp = highTemp,
            lowTemp = lowTemp,
            location = location,
            timestamp = timestamp,
            iconUrl = iconUrl
        )
    }
}
