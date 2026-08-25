package com.quantumslate.dashboard.domain.model

data class Weather(
    val temperature: Double,
    val condition: String,
    val highTemp: Double,
    val lowTemp: Double,
    val location: String,
    val timestamp: Long,
    val iconUrl: String? = null,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0
)

data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val calendarName: String,
    val color: Int,
    val isAllDay: Boolean = false
)

data class NewsArticle(
    val guid: String,
    val title: String,
    val description: String?,
    val link: String,
    val pubDate: Long,
    val source: String,
    val imageUrl: String? = null
)

data class FlightStatus(
    val flightNumber: String,
    val airline: String,
    val departureAirport: String,
    val arrivalAirport: String,
    val scheduledDeparture: Long,
    val scheduledArrival: Long,
    val estimatedDeparture: Long?,
    val estimatedArrival: Long?,
    val status: String,
    val gate: String?,
    val terminal: String?
)

data class SpotifyTrack(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String?,
    val isPlaying: Boolean,
    val progressMs: Long,
    val durationMs: Long
)

enum class MascotMood {
    HAPPY,
    NEUTRAL,
    CONCERNED,
    EXCITED,
    SLEEPY
}

data class MascotState(
    val mood: MascotMood,
    val character: String,
    val animation: String,
    val accessories: List<String> = emptyList()
)
