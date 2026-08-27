package com.quantumslate.dashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0,
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

@Entity(tableName = "calendar_event")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val calendarName: String,
    val color: Int,
    val isAllDay: Boolean = false
)

@Entity(tableName = "news_article")
data class NewsArticleEntity(
    @PrimaryKey val guid: String,
    val title: String,
    val description: String?,
    val link: String,
    val pubDate: Long,
    val source: String,
    val imageUrl: String? = null
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "flight")
data class FlightEntity(
    @PrimaryKey val flightNumber: String,
    val airline: String,
    val departureAirport: String,
    val arrivalAirport: String,
    val scheduledDeparture: Long,
    val scheduledArrival: Long,
    val estimatedDeparture: Long?,
    val estimatedArrival: Long?,
    val status: String,
    val gate: String?,
    val terminal: String?,
    val timestamp: Long
)

@Entity(tableName = "spotify_track")
data class SpotifyTrackEntity(
    @PrimaryKey val id: String = "current",
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val albumArtUrl: String?,
    val isPlaying: Boolean,
    val progressMs: Long,
    val durationMs: Long,
    val timestamp: Long
)

@Entity(tableName = "mascot_state")
data class MascotStateEntity(
    @PrimaryKey val id: Int = 0,
    val mood: String,
    val animation: String,
    val character: String,
    val lastUpdate: Long
)
