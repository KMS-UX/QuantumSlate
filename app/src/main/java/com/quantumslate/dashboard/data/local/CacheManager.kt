package com.quantumslate.dashboard.data.local

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages cache staleness and expiration for dashboard data.
 * Provides utilities for checking if cached data is fresh or stale.
 */
@Singleton
class CacheManager @Inject constructor(
    private val context: Context
) {
    companion object {
        // Cache expiration thresholds
        const val FRESH_THRESHOLD_MS = 30 * 60 * 1000L      // 30 minutes - considered fresh
        const val STALE_THRESHOLD_MS = 24 * 60 * 60 * 1000L // 24 hours - considered stale
        const val EXPIRED_THRESHOLD_MS = 48 * 60 * 60 * 1000L // 48 hours - consider expired
        
        // Per-widget cache durations (in milliseconds)
        const val WEATHER_CACHE_DURATION = 30 * 60 * 1000L     // 30 min
        const val NEWS_CACHE_DURATION = 2 * 60 * 60 * 1000L    // 2 hours
        const val FLIGHT_CACHE_DURATION = 5 * 60 * 1000L       // 5 min (when active)
        const val SPOTIFY_CACHE_DURATION_PLAYING = 30 * 1000L  // 30 sec
        const val SPOTIFY_CACHE_DURATION_PAUSED = 5 * 60 * 1000L // 5 min
        const val CALENDAR_CACHE_DURATION = 15 * 60 * 1000L    // 15 min
    }
    
    private val _cacheStatus = MutableStateFlow(CacheStatus(isLoading = true))
    val cacheStatus: StateFlow<CacheStatus> = _cacheStatus
    
    private val _globalCacheStatus = MutableStateFlow<Map<String, CacheInfo>>(emptyMap())
    val globalCacheStatus: StateFlow<Map<String, CacheInfo>> = _globalCacheStatus.asStateFlow()
    
    private val widgetTimestamps = mutableMapOf<String, Long>()
    
    /**
     * Cache information for a specific widget
     */
    data class CacheInfo(
        val lastUpdated: Long,
        val level: CacheLevel,
        val humanReadableTime: String
    )
    
    /**
     * Updates the timestamp for a specific widget
     */
    fun updateWidgetTimestamp(widgetKey: String, timestamp: Long = System.currentTimeMillis()) {
        widgetTimestamps[widgetKey] = timestamp
        updateGlobalCacheStatus()
    }
    
    /**
     * Gets the last update time for a widget
     */
    fun getLastUpdateTime(widgetKey: String): Long? {
        return widgetTimestamps[widgetKey]
    }
    
    /**
     * Gets human-readable time since last update
     */
    fun getHumanReadableTime(timestamp: Long): String {
        return getLastUpdatedString(timestamp)
    }
    
    private fun updateGlobalCacheStatus() {
        val statusMap = widgetTimestamps.mapValues { (key, timestamp) ->
            CacheInfo(
                lastUpdated = timestamp,
                level = getCacheLevel(timestamp),
                humanReadableTime = getHumanReadableTime(timestamp)
            )
        }
        _globalCacheStatus.value = statusMap
    }
    
    /**
     * Checks if weather data is fresh based on timestamp.
     */
    fun isWeatherFresh(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < WEATHER_CACHE_DURATION
    }
    
    /**
     * Checks if news data is fresh based on timestamp.
     */
    fun isNewsFresh(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < NEWS_CACHE_DURATION
    }
    
    /**
     * Checks if flight data is fresh based on timestamp.
     * For active flights, uses shorter cache duration.
     */
    fun isFlightFresh(timestamp: Long, isActiveFlight: Boolean = false): Boolean {
        val age = System.currentTimeMillis() - timestamp
        val duration = if (isActiveFlight) FLIGHT_CACHE_DURATION else NEWS_CACHE_DURATION
        return age < duration
    }
    
    /**
     * Checks if Spotify data is fresh based on playback state.
     */
    fun isSpotifyFresh(timestamp: Long, isPlaying: Boolean): Boolean {
        val age = System.currentTimeMillis() - timestamp
        val duration = if (isPlaying) SPOTIFY_CACHE_DURATION_PLAYING else SPOTIFY_CACHE_DURATION_PAUSED
        return age < duration
    }
    
    /**
     * Gets human-readable "last updated" string.
     */
    fun getLastUpdatedString(timestamp: Long): String {
        val age = System.currentTimeMillis() - timestamp
        
        return when {
            age < 60 * 1000L -> "Just now"
            age < 60 * 60 * 1000L -> "${age / (60 * 1000L)} min ago"
            age < 24 * 60 * 60 * 1000L -> "${age / (60 * 60 * 1000L)} hours ago"
            else -> "${age / (24 * 60 * 60 * 1000L)} days ago"
        }
    }
    
    /**
     * Determines cache status level based on age.
     */
    fun getCacheLevel(timestamp: Long): CacheLevel {
        val age = System.currentTimeMillis() - timestamp
        
        return when {
            age < FRESH_THRESHOLD_MS -> CacheLevel.FRESH
            age < STALE_THRESHOLD_MS -> CacheLevel.STALE
            age < EXPIRED_THRESHOLD_MS -> CacheLevel.EXPIRED
            else -> CacheLevel.VERY_OLD
        }
    }
    
    /**
     * Formats cache age with appropriate warning indicator.
     */
    fun getCacheAgeDisplay(timestamp: Long): CacheAgeDisplay {
        val level = getCacheLevel(timestamp)
        val lastUpdated = getLastUpdatedString(timestamp)
        
        return CacheAgeDisplay(
            text = lastUpdated,
            level = level,
            showWarning = level != CacheLevel.FRESH,
            shouldRefresh = level == CacheLevel.EXPIRED || level == CacheLevel.VERY_OLD
        )
    }
    
    /**
     * Updates global cache status.
     */
    fun updateCacheStatus(isLoading: Boolean, hasError: Boolean = false, errorMessage: String? = null) {
        _cacheStatus.value = CacheStatus(
            isLoading = isLoading,
            hasError = hasError,
            errorMessage = errorMessage
        )
    }
}

/**
 * Represents the freshness level of cached data.
 */
enum class CacheLevel {
    FRESH,      // Recently updated, fully reliable
    STALE,      // Older but still usable
    EXPIRED,    // Very old, should refresh soon
    VERY_OLD    // Extremely old, data may be inaccurate
}

/**
 * Display information for cache age.
 */
data class CacheAgeDisplay(
    val text: String,
    val level: CacheLevel,
    val showWarning: Boolean,
    val shouldRefresh: Boolean
)

/**
 * Global cache status for the dashboard.
 */
data class CacheStatus(
    val isLoading: Boolean,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val lastSuccessfulUpdate: Long = System.currentTimeMillis()
)
