package com.quantumslate.dashboard.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quantumslate.dashboard.data.local.CacheExpiry
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.repository.FlightRepository
import com.quantumslate.dashboard.data.repository.MascotRepository
import com.quantumslate.dashboard.data.repository.NewsRepository
import com.quantumslate.dashboard.data.repository.SpotifyRepository
import com.quantumslate.dashboard.data.repository.WeatherLocationResolver
import com.quantumslate.dashboard.data.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

/**
 * Worker responsible for fetching and caching all dashboard data.
 * Runs periodically based on update frequency settings.
 */
@HiltWorker
class DashboardUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val newsRepository: NewsRepository,
    private val flightRepository: FlightRepository,
    private val spotifyRepository: SpotifyRepository,
    private val mascotRepository: MascotRepository,
    private val preferencesManager: PreferencesManager,
    private val cacheExpiry: CacheExpiry,
    private val weatherLocationResolver: WeatherLocationResolver
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Bible §6: drop anything past its useful life before fetching new data.
            cacheExpiry.purgeExpired()

            // Fetch weather data
            fetchWeather()
            
            // Fetch RSS news feeds
            fetchNews()
            
            // Fetch flight status for tracked flights
            fetchFlightStatus()
            
            // Fetch Spotify playback status
            fetchSpotifyStatus()
            
            // Update mascot mood based on all data
            updateMascotMood()
            
            Result.success()
        } catch (e: Exception) {
            // Log error but return success to continue periodic updates
            Result.retry()
        }
    }

    private suspend fun fetchWeather() {
        try {
            val location = preferencesManager.getLocation()
            if (!location.isNullOrBlank()) {
                // Try to fetch by location name
                weatherRepository.fetchWeatherByLocationName(location)
            } else {
                // Use default coordinates (can be customized)
                weatherLocationResolver.fetchForCurrentLocation()
            }
        } catch (e: Exception) {
            // Silently fail - will retry next update
        }
    }

    private suspend fun fetchNews() {
        try {
            val rssFeeds = preferencesManager.getRssFeeds()
            if (rssFeeds.isNullOrEmpty()) {
                // Use default news feed if none configured
                val defaultFeed = "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml"
                newsRepository.fetchAndCacheRssFeed(defaultFeed)
            } else {
                // Fetch from all configured feeds
                rssFeeds.forEach { feedUrl ->
                    newsRepository.fetchAndCacheRssFeed(feedUrl)
                }
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }

    private suspend fun fetchFlightStatus() {
        try {
            val trackedFlights = preferencesManager.getTrackedFlights()
            if (!trackedFlights.isNullOrEmpty()) {
                trackedFlights.forEach { flightNumber ->
                    flightRepository.fetchAndCacheFlightStatus(flightNumber.trim())
                }
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }

    private suspend fun fetchSpotifyStatus() {
        try {
            val spotifyEnabled = preferencesManager.isSpotifyEnabled()
            if (spotifyEnabled) {
                spotifyRepository.fetchAndCachePlayback()
            }
        } catch (e: Exception) {
            // Silently fail - Spotify may not be authenticated
        }
    }

    private suspend fun updateMascotMood() {
        try {
            mascotRepository.updateMascotMood()
        } catch (e: Exception) {
            // Silently fail
        }
    }
}
