package com.quantumslate.dashboard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE id = 0")
    fun getWeather(): Flow<WeatherEntity?>

    @Query("SELECT * FROM weather WHERE id = 0")
    suspend fun getWeatherOnce(): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather")
    suspend fun clearWeather()
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_event ORDER BY startTime ASC LIMIT 3")
    fun getUpcomingEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_event ORDER BY startTime ASC LIMIT 3")
    suspend fun getUpcomingEventsOnce(): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_event")
    suspend fun clearEvents()
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_article ORDER BY pubDate DESC LIMIT 10")
    fun getNewsArticles(): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_article ORDER BY pubDate DESC LIMIT 10")
    suspend fun getNewsArticlesOnce(): List<NewsArticleEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    @Query("DELETE FROM news_article")
    suspend fun clearArticles()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingsEntity?

    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: List<SettingsEntity>)
}

@Dao
interface FlightDao {
    @Query("SELECT * FROM flight ORDER BY scheduledDeparture ASC LIMIT 2")
    fun getTrackedFlights(): Flow<List<FlightEntity>>

    @Query("SELECT * FROM flight WHERE flightNumber = :flightNumber")
    suspend fun getFlightOnce(flightNumber: String): FlightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlight(flight: FlightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlights(flights: List<FlightEntity>)

    @Query("DELETE FROM flight WHERE flightNumber = :flightNumber")
    suspend fun deleteFlight(flightNumber: String)

    @Query("DELETE FROM flight")
    suspend fun clearAllFlights()
}

@Dao
interface SpotifyDao {
    @Query("SELECT * FROM spotify_track WHERE id = 'current'")
    fun getCurrentTrack(): Flow<SpotifyTrackEntity?>

    @Query("SELECT * FROM spotify_track WHERE id = 'current'")
    suspend fun getCurrentTrackOnce(): SpotifyTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: SpotifyTrackEntity)

    @Query("DELETE FROM spotify_track")
    suspend fun clearTracks()
}

@Dao
interface MascotStateDao {
    @Query("SELECT * FROM mascot_state WHERE id = 0")
    fun getMascotState(): Flow<MascotStateEntity?>

    @Query("SELECT * FROM mascot_state WHERE id = 0")
    suspend fun getMascotStateOnce(): MascotStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMascotState(state: MascotStateEntity)
}
