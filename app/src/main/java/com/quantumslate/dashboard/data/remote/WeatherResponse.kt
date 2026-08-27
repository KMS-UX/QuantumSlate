package com.quantumslate.dashboard.data.remote

import com.google.gson.annotations.SerializedName
import com.quantumslate.dashboard.domain.model.Weather

/**
 * Response body of OpenWeatherMap `GET /data/2.5/weather`.
 *
 * Only the fields the dashboard actually renders are modelled; Gson ignores the rest.
 */
data class WeatherResponse(
    @SerializedName("name") val name: String? = null,
    @SerializedName("dt") val dt: Long = 0,
    @SerializedName("weather") val weather: List<WeatherCondition> = emptyList(),
    @SerializedName("main") val main: MainWeather? = null,
    @SerializedName("wind") val wind: WindInfo? = null
) {
    /**
     * OpenWeatherMap reports `dt` in seconds; the app works in millis throughout.
     */
    fun toDomainModel(): Weather {
        val condition = weather.firstOrNull()
        return Weather(
            temperature = main?.temp ?: 0.0,
            condition = condition?.description ?: condition?.main ?: "Unknown",
            highTemp = main?.tempMax ?: main?.temp ?: 0.0,
            lowTemp = main?.tempMin ?: main?.temp ?: 0.0,
            location = name.orEmpty(),
            timestamp = if (dt > 0) dt * 1000L else System.currentTimeMillis(),
            iconUrl = condition?.icon?.let { "https://openweathermap.org/img/wn/$it@2x.png" },
            humidity = main?.humidity ?: 0,
            windSpeed = wind?.speed ?: 0.0
        )
    }
}

data class WeatherCondition(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("main") val main: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("icon") val icon: String? = null
)

data class MainWeather(
    @SerializedName("temp") val temp: Double = 0.0,
    @SerializedName("feels_like") val feelsLike: Double = 0.0,
    @SerializedName("temp_min") val tempMin: Double? = null,
    @SerializedName("temp_max") val tempMax: Double? = null,
    @SerializedName("pressure") val pressure: Int = 0,
    @SerializedName("humidity") val humidity: Int = 0
)

data class WindInfo(
    @SerializedName("speed") val speed: Double = 0.0,
    @SerializedName("deg") val deg: Int = 0
)
