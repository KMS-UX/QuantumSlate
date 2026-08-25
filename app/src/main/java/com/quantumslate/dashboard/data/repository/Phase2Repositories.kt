package com.quantumslate.dashboard.data.repository

import android.content.Context
import com.quantumslate.dashboard.data.local.FlightDao
import com.quantumslate.dashboard.data.local.MascotStateDao
import com.quantumslate.dashboard.data.local.NewsDao
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.local.SpotifyDao
import com.quantumslate.dashboard.data.local.SpotifyTrackEntity
import com.quantumslate.dashboard.data.remote.ApiClient
import com.quantumslate.dashboard.data.remote.FlightEntry
import com.quantumslate.dashboard.data.remote.FlightStatusResponse
import com.quantumslate.dashboard.data.local.FlightEntity
import com.quantumslate.dashboard.domain.model.MascotMood
import com.quantumslate.dashboard.domain.model.MascotState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

// ==================== RSS NEWS REPOSITORY ====================

@Singleton
class NewsRepository @Inject constructor(
    private val newsDao: NewsDao,
    private val preferencesManager: PreferencesManager
) {
    val newsArticles = newsDao.getNewsArticles().flowOn(Dispatchers.IO)

    /**
     * Fetch and cache news from all configured RSS feeds
     */
    suspend fun fetchAndCacheNews(): Result<List<com.quantumslate.dashboard.data.local.NewsArticleEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val feedUrls = preferencesManager.getRssFeedUrls()
                if (feedUrls.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                var totalArticles = 0
                feedUrls.forEach { feedUrl ->
                    fetchAndCacheRssFeed(feedUrl).onSuccess { count ->
                        totalArticles += count
                    }
                }
                
                val allArticles = getCachedNews()
                Result.success(allArticles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchAndCacheRssFeed(feedUrl: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val apiService = ApiClient.getRssApiService(feedUrl)
                val document = apiService.getRssFeed(feedUrl)
                
                // Parse RSS XML manually (simplified approach)
                val articles = parseRssDocument(document, feedUrl)
                
                if (articles.isEmpty()) {
                    return@withContext Result.failure(Exception("No articles found in feed"))
                }
                
                newsDao.insertArticles(articles)
                Result.success(articles.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseRssDocument(document: Document, sourceUrl: String): List<com.quantumslate.dashboard.data.local.NewsArticleEntity> {
        val articles = mutableListOf<com.quantumslate.dashboard.data.local.NewsArticleEntity>()
        
        try {
            // Simple RSS 2.0 parsing - look for item elements
            val items = document.getElementsByTagName("item")
            
            for (i in 0 until minOf(items.length, 10)) {
                val item = items.item(i)
                val titleNodes = item.getElementsByTagName("title")
                val linkNodes = item.getElementsByTagName("link")
                val descNodes = item.getElementsByTagName("description")
                val pubDateNodes = item.getElementsByTagName("pubDate")
                
                val title = titleNodes.item(0)?.textContent ?: "No Title"
                val link = linkNodes.item(0)?.textContent ?: "#"
                val description = descNodes.item(0)?.textContent
                val pubDateStr = pubDateNodes.item(0)?.textContent
                
                val pubDate = try {
                    pubDateStr?.let { 
                        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(it)?.time 
                    } ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
                
                // Generate a unique GUID from the link
                val guid = link.hashCode().toString()
                
                articles.add(
                    com.quantumslate.dashboard.data.local.NewsArticleEntity(
                        guid = guid,
                        title = title,
                        description = description,
                        link = link,
                        pubDate = pubDate,
                        source = sourceUrl.substringAfter("://").substringBefore("/").take(50),
                        imageUrl = null
                    )
                )
            }
        } catch (e: Exception) {
            // Log error but continue with empty list
        }
        
        return articles
    }

    suspend fun getCachedNews(): List<com.quantumslate.dashboard.data.local.NewsArticleEntity> {
        return withContext(Dispatchers.IO) {
            newsDao.getNewsArticlesOnce()
        }
    }
}

// ==================== FLIGHT STATUS REPOSITORY ====================

@Singleton
class FlightRepository @Inject constructor(
    private val flightDao: FlightDao,
    private val preferencesManager: PreferencesManager
) {
    val trackedFlights: Flow<List<FlightEntity>> = flightDao.getTrackedFlights().flowOn(Dispatchers.IO)

    suspend fun fetchAndCacheFlightStatus(flightNumber: String): Result<FlightEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = preferencesManager.getFlightApiKey()
                if (apiKey.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Flight API key not configured"))
                }

                // Use AviationEdge by default
                val apiService = ApiClient.getFlightApiService(com.quantumslate.dashboard.data.remote.FlightApiService.BASE_URL_AVIATION_EDGE)
                val response = apiService.getFlightStatus(flightNumber, apiKey)
                
                val flightEntity = response.toFlightEntity(flightNumber, apiKey)
                flightDao.insertFlight(flightEntity)
                
                Result.success(flightEntity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun trackFlight(flightNumber: String) {
        withContext(Dispatchers.IO) {
            fetchAndCacheFlightStatus(flightNumber)
        }
    }

    suspend fun untrackFlight(flightNumber: String) {
        withContext(Dispatchers.IO) {
            flightDao.deleteFlight(flightNumber)
        }
    }

    suspend fun getCachedFlights(): List<FlightEntity> {
        return withContext(Dispatchers.IO) {
            flightDao.getTrackedFlights().firstOrNull() ?: emptyList()
        }
    }
    
    /**
     * Fetch status for all tracked flights
     */
    suspend fun fetchAllTrackedFlights(): Result<List<FlightEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val tracked = getCachedFlights()
                if (tracked.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                // Fetch updates for all tracked flights
                tracked.forEach { flight ->
                    fetchAndCacheFlightStatus(flight.flightNumber).onSuccess { updated ->
                        flightDao.insertFlight(updated)
                    }
                }
                
                Result.success(getCachedFlights())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

private fun FlightStatusResponse.toFlightEntity(flightNumber: String, apiKey: String): FlightEntity {
    val now = System.currentTimeMillis()
    
    // Parse times - AviationEdge returns ISO format strings
    val scheduledDep = departure?.scheduledTime?.let { parseIsoTime(it) } ?: now
    val scheduledArr = arrival?.scheduledTime?.let { parseIsoTime(it) } ?: now
    val estimatedDep = departure?.estimatedTime?.let { parseIsoTime(it) }
    val estimatedArr = arrival?.estimatedTime?.let { parseIsoTime(it) }
    
    return FlightEntity(
        flightNumber = flightNumber,
        airline = airline?.name ?: "Unknown Airline",
        departureAirport = departure?.name ?: departure?.code?.iata ?: "Unknown",
        arrivalAirport = arrival?.name ?: arrival?.code?.iata ?: "Unknown",
        scheduledDeparture = scheduledDep,
        scheduledArrival = scheduledArr,
        estimatedDeparture = estimatedDep,
        estimatedArrival = estimatedArr,
        status = status ?: "Unknown",
        gate = departure?.gate,
        terminal = departure?.terminal,
        timestamp = now
    )
}

private fun parseIsoTime(isoString: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        format.timeZone = TimeZone.getTimeZone("UTC")
        format.parse(isoString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

// ==================== SPOTIFY REPOSITORY ====================

@Singleton
class SpotifyRepository @Inject constructor(
    private val spotifyDao: SpotifyDao,
    private val preferencesManager: PreferencesManager
) {
    val currentTrack: Flow<SpotifyTrackEntity?> = spotifyDao.getCurrentTrack().flowOn(Dispatchers.IO)

    suspend fun fetchAndCachePlayback(): Result<SpotifyTrackEntity?> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferencesManager.getSpotifyAccessToken()
                if (token.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Spotify not authenticated"))
                }

                val apiService = ApiClient.getSpotifyApiService()
                val response = try {
                    apiService.getCurrentPlayback("Bearer $token")
                } catch (e: Exception) {
                    // Try fallback endpoint
                    apiService.getCurrentlyPlaying("Bearer $token")
                }

                val trackEntity = response.toSpotifyTrackEntity(token)
                if (trackEntity != null) {
                    spotifyDao.insertTrack(trackEntity)
                }

                Result.success(trackEntity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCachedTrack(): SpotifyTrackEntity? {
        return withContext(Dispatchers.IO) {
            spotifyDao.getCurrentTrackOnce()
        }
    }
}

private fun com.quantumslate.dashboard.data.remote.SpotifyPlaybackResponse.toSpotifyTrackEntity(token: String): SpotifyTrackEntity? {
    val item = this.item ?: return null
    
    return SpotifyTrackEntity(
        id = "current",
        trackName = item.name,
        artistName = item.artists.joinToString(", ") { it.name },
        albumName = item.album.name,
        albumArtUrl = item.album.images.maxByOrNull { it.width }?.url,
        isPlaying = this.is_playing,
        progressMs = this.progress_ms,
        durationMs = item.duration_ms,
        timestamp = System.currentTimeMillis()
    )
}

private fun com.quantumslate.dashboard.data.remote.SpotifyTrackResponse.toSpotifyTrackEntity(token: String): SpotifyTrackEntity? {
    val item = this.item ?: return null
    
    return SpotifyTrackEntity(
        id = "current",
        trackName = item.name,
        artistName = item.artists.joinToString(", ") { it.name },
        albumName = item.album.name,
        albumArtUrl = item.album.images.maxByOrNull { it.width }?.url,
        isPlaying = this.is_playing,
        progressMs = this.progress_ms,
        durationMs = item.duration_ms,
        timestamp = System.currentTimeMillis()
    )
}

// ==================== MASCOT STATE REPOSITORY ====================

@Singleton
class MascotRepository @Inject constructor(
    private val mascotDao: MascotStateDao,
    private val preferencesManager: PreferencesManager,
    private val weatherRepository: WeatherRepository,
    private val flightRepository: FlightRepository,
    private val spotifyRepository: SpotifyRepository
) {
    val mascotState: Flow<MascotStateEntity?> = mascotDao.getMascotState().flowOn(Dispatchers.IO)

    suspend fun updateMascotMood() {
        withContext(Dispatchers.IO) {
            try {
                // Gather data to determine mood
                val weather = weatherRepository.getCachedWeather()
                val flights = flightRepository.getCachedFlights()
                val track = spotifyRepository.getCachedTrack()
                
                val character = preferencesManager.getMascotCharacter() ?: "robot"
                val animationsEnabled = preferencesManager.areMascotAnimationsEnabled()
                
                // Calculate mood based on data
                val mood = calculateMood(weather, flights, track)
                val animation = determineAnimation(mood, animationsEnabled)
                
                val state = MascotStateEntity(
                    id = 0,
                    mood = mood.name,
                    animation = animation,
                    character = character,
                    lastUpdate = System.currentTimeMillis()
                )
                
                mascotDao.saveMascotState(state)
            } catch (e: Exception) {
                // Keep previous state on error
            }
        }
    }

    private fun calculateMood(
        weather: com.quantumslate.dashboard.domain.model.Weather?,
        flights: List<FlightEntity>,
        track: SpotifyTrackEntity?
    ): MascotMood {
        // Default mood
        var mood = MascotMood.NEUTRAL
        
        // Weather affects mood
        weather?.condition?.lowercase()?.let { condition ->
            when {
                condition.contains("sun") || condition.contains("clear") -> mood = MascotMood.HAPPY
                condition.contains("rain") || condition.contains("storm") -> mood = MascotMood.CONCERNED
                condition.contains("snow") -> mood = MascotMood.EXCITED
            }
        }
        
        // Flight status affects mood
        flights.forEach { flight ->
            val statusLower = flight.status.lowercase()
            if (statusLower.contains("delayed") || statusLower.contains("cancelled")) {
                mood = MascotMood.CONCERNED
            } else if (statusLower.contains("on time") || statusLower.contains("landed")) {
                mood = MascotMood.HAPPY
            }
        }
        
        // Music makes mascot happy
        if (track?.isPlaying == true) {
            mood = MascotMood.EXCITED
        }
        
        return mood
    }

    private fun determineAnimation(mood: MascotMood, animationsEnabled: Boolean): String {
        if (!animationsEnabled) return "idle_static"
        
        return when (mood) {
            MascotMood.HAPPY -> "happy_wave"
            MascotMood.EXCITED -> "dancing"
            MascotMood.CONCERNED -> "worried_look"
            MascotMood.SLEEPY -> "sleeping"
            MascotMood.NEUTRAL -> "idle_breathing"
        }
    }

    suspend fun getCachedMascotState(): MascotStateEntity? {
        return withContext(Dispatchers.IO) {
            mascotDao.getMascotStateOnce()
        }
    }
}
