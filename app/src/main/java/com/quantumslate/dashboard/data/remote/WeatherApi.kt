package com.quantumslate.dashboard.data.remote

import com.quantumslate.dashboard.domain.model.Weather
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url
import org.w3c.dom.Document

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }
}

interface FlightApiService {
    companion object {
        const val BASE_URL_AVIATION_EDGE = "https://aviation-edge.com/v2/public/"
        const val BASE_URL_FLIGHT_AWARE = "https://aeroapi.flightaware.com/aeroapi/"
    }

    @GET("flightStatus")
    suspend fun getFlightStatus(
        @Query("flightNumber") flightNumber: String,
        @Query("key") apiKey: String
    ): FlightStatusResponse

    @GET("timetable")
    suspend fun getTimetable(
        @Query("type") type: String,
        @Query("iataCode") iataCode: String,
        @Query("key") apiKey: String
    ): TimetableResponse
}

interface SpotifyApiService {
    companion object {
        const val BASE_URL = "https://api.spotify.com/v1/"
    }

    @GET("me/player")
    suspend fun getCurrentPlayback(
        @Header("Authorization") token: String
    ): SpotifyPlaybackResponse

    @GET("me/player/currently-playing")
    suspend fun getCurrentlyPlaying(
        @Header("Authorization") token: String
    ): SpotifyTrackResponse
}

interface RssApiService {
    @GET(".")
    suspend fun getRssFeed(
        @Url url: String
    ): Document
}

// ==================== FLIGHT MODELS ====================

data class FlightStatusResponse(
    val flightNumber: String?,
    val airline: AirlineInfo?,
    val departure: AirportInfo?,
    val arrival: AirportInfo?,
    val status: String?,
    val live: LiveFlightData?
)

data class AirlineInfo(
    val name: String?,
    val code: CodeInfo?
)

data class CodeInfo(
    val iata: String?,
    val icao: String?
)

data class AirportInfo(
    val name: String?,
    val code: CodeInfo?,
    val scheduledTime: String?,
    val estimatedTime: String?,
    val actualTime: String?,
    val terminal: String?,
    val gate: String?
)

data class LiveFlightData(
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    val speed: Double?,
    val heading: Int?
)

data class TimetableResponse(
    val timetable: List<FlightEntry>
)

data class FlightEntry(
    val flightNumber: String?,
    val departure: AirportInfo?,
    val arrival: AirportInfo?,
    val status: String?
)

// ==================== SPOTIFY MODELS ====================

data class SpotifyPlaybackResponse(
    val device: SpotifyDevice?,
    val is_playing: Boolean,
    val item: SpotifyItem?,
    val progress_ms: Long,
    val duration_ms: Long
)

data class SpotifyDevice(
    val id: String,
    val name: String,
    val type: String,
    val is_active: Boolean
)

data class SpotifyItem(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum,
    val duration_ms: Long
)

data class SpotifyArtist(
    val id: String,
    val name: String
)

data class SpotifyAlbum(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    val url: String,
    val height: Int,
    val width: Int
)

data class SpotifyTrackResponse(
    val is_playing: Boolean,
    val item: SpotifyItem?,
    val progress_ms: Long
)
