package com.quantumslate.dashboard.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumslate.dashboard.data.local.CacheExpiry
import com.quantumslate.dashboard.data.local.CacheManager
import com.quantumslate.dashboard.data.local.CalendarEventEntity
import com.quantumslate.dashboard.data.local.DashboardWidget
import com.quantumslate.dashboard.data.local.WidgetLayout
import com.quantumslate.dashboard.data.local.FlightEntity
import com.quantumslate.dashboard.data.local.MascotStateEntity
import com.quantumslate.dashboard.data.local.NewsArticleEntity
import com.quantumslate.dashboard.data.local.CacheLevel
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.local.SpotifyTrackEntity
import com.quantumslate.dashboard.data.repository.CalendarRepository
import com.quantumslate.dashboard.data.repository.FlightRepository
import com.quantumslate.dashboard.data.repository.MascotRepository
import com.quantumslate.dashboard.data.repository.NewsRepository
import com.quantumslate.dashboard.data.repository.SpotifyRepository
import com.quantumslate.dashboard.data.repository.WeatherRepository
import com.quantumslate.dashboard.domain.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Comprehensive UI state for the entire dashboard
 */
data class DashboardUiState(
    // Weather
    val weather: Weather? = null,
    val isWeatherLoading: Boolean = false,
    val weatherError: String? = null,
    val weatherLastUpdated: Long? = null,
    
    // News
    val newsArticles: List<NewsArticleEntity> = emptyList(),
    val isNewsLoading: Boolean = false,
    val newsError: String? = null,
    val newsLastUpdated: Long? = null,
    
    // Flights
    val flights: List<FlightEntity> = emptyList(),
    val isFlightsLoading: Boolean = false,
    val flightsError: String? = null,
    val flightsLastUpdated: Long? = null,
    
    // Spotify
    val spotifyTrack: SpotifyTrackEntity? = null,
    val isSpotifyLoading: Boolean = false,
    val spotifyError: String? = null,
    val spotifyLastUpdated: Long? = null,
    
    // Calendar
    val calendarEvents: List<CalendarEventEntity> = emptyList(),
    val isCalendarLoading: Boolean = false,
    val calendarError: String? = null,
    val calendarLastUpdated: Long? = null,
    val calendarPermissionMissing: Boolean = false,

    // Flight request allowance
    val flightRequestsRemaining: Int? = null,

    // Mascot
    val mascotState: MascotStateEntity? = null,
    val mascotAnimationsEnabled: Boolean = true,

    // Appearance
    val darkMode: PreferencesManager.DarkMode = PreferencesManager.DarkMode.AUTO,

    // Which widgets are shown, and in what order (Bible §5)
    val widgetLayout: WidgetLayout = WidgetLayout(),
    
    // Global
    val isRefreshing: Boolean = false
) {
    /**
     * Calculate overall data freshness status
     */
    fun getOverallFreshness(cacheManager: CacheManager): CacheLevel {
        val timestamps = listOfNotNull(
            weatherLastUpdated,
            newsLastUpdated,
            flightsLastUpdated,
            spotifyLastUpdated
        )
        
        if (timestamps.isEmpty()) return CacheLevel.EXPIRED
        
        val oldestTimestamp = timestamps.minOrNull() ?: return CacheLevel.EXPIRED
        val ageMs = System.currentTimeMillis() - oldestTimestamp
        
        return when {
            ageMs < 5 * 60 * 1000 -> CacheLevel.FRESH      // < 5 min
            ageMs < 30 * 60 * 1000 -> CacheLevel.STALE     // < 30 min
            ageMs < 2 * 60 * 60 * 1000 -> CacheLevel.EXPIRED // < 2 hours
            else -> CacheLevel.VERY_OLD                    // > 2 hours
        }
    }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val newsRepository: NewsRepository,
    private val flightRepository: FlightRepository,
    private val spotifyRepository: SpotifyRepository,
    private val calendarRepository: CalendarRepository,
    private val cacheExpiry: CacheExpiry,
    private val mascotRepository: MascotRepository,
    private val preferencesManager: PreferencesManager,
    private val cacheManager: CacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _cacheStatus = MutableStateFlow<Map<String, CacheManager.CacheInfo>>(emptyMap())
    val cacheStatus: StateFlow<Map<String, CacheManager.CacheInfo>> = _cacheStatus.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            darkMode = preferencesManager.getDarkMode(),
            widgetLayout = preferencesManager.getWidgetLayout()
        )
        viewModelScope.launch { cacheExpiry.purgeExpired() }
        loadAllData()
        observeCacheStatus()
        updateMascotMood()
    }

    /**
     * Load all dashboard data initially
     */
    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            // Each loader launches its own coroutine and tracks its own loading flag,
            // so these all run concurrently.
            loadWeather()
            loadNews()
            loadCalendar()
            loadFlights()
            loadSpotify()
            loadMascot()

            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    /**
     * Refresh specific widget data
     */
    fun refreshWidget(widgetType: WidgetType) {
        when (widgetType) {
            WidgetType.WEATHER -> loadWeather()
            WidgetType.CALENDAR -> loadCalendar()
            WidgetType.NEWS -> loadNews()
            WidgetType.FLIGHTS -> loadFlights(force = true)
            WidgetType.SPOTIFY -> loadSpotify()
            WidgetType.MASCOT -> loadMascot()
        }
    }

    /**
     * Refresh all widgets
     */
    fun refreshAll() {
        loadAllData()
    }

    // ==================== WEATHER ====================

    private fun loadWeather() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true)
            
            try {
                val location = preferencesManager.getLocation()
                val result = if (!location.isNullOrEmpty()) {
                    weatherRepository.fetchWeatherByLocationName(location)
                } else {
                    weatherRepository.fetchAndCacheWeather(40.7128, -74.0060) // Default: NYC
                }
                
                result.onSuccess { weather ->
                    _uiState.value = _uiState.value.copy(
                        weather = weather,
                        isWeatherLoading = false,
                        weatherError = null,
                        weatherLastUpdated = System.currentTimeMillis()
                    )
                    updateMascotMood()
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isWeatherLoading = false,
                        weatherError = error.message ?: "Failed to fetch weather"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isWeatherLoading = false,
                    weatherError = e.message ?: "Unknown error"
                )
            }
        }
    }

    // ==================== NEWS ====================

    private fun loadNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isNewsLoading = true)
            
            try {
                val result = newsRepository.fetchAndCacheNews()
                result.onSuccess { articles ->
                    _uiState.value = _uiState.value.copy(
                        newsArticles = articles,
                        isNewsLoading = false,
                        newsError = null,
                        newsLastUpdated = System.currentTimeMillis()
                    )
                }.onFailure { error ->
                    // Try to load cached data on error
                    val cached = newsRepository.getCachedNews()
                    _uiState.value = _uiState.value.copy(
                        newsArticles = cached,
                        isNewsLoading = false,
                        newsError = if (cached.isEmpty()) error.message else null,
                        newsLastUpdated = cacheManager.getLastUpdateTime("news")
                    )
                }
            } catch (e: Exception) {
                val cached = newsRepository.getCachedNews()
                _uiState.value = _uiState.value.copy(
                    newsArticles = cached,
                    isNewsLoading = false,
                    newsError = e.message,
                    newsLastUpdated = cacheManager.getLastUpdateTime("news")
                )
            }
        }
    }

    // ==================== FLIGHTS ====================

    private fun loadFlights(force: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFlightsLoading = true)
            
            try {
                val result = flightRepository.fetchAllTrackedFlights(force = force)
                result.onSuccess { flights ->
                    _uiState.value = _uiState.value.copy(
                        flights = flights,
                        isFlightsLoading = false,
                        flightsError = null,
                        flightsLastUpdated = System.currentTimeMillis(),
                        flightRequestsRemaining = flightRepository.remainingRequests()
                    )
                    updateMascotMood()
                }.onFailure { error ->
                    val cached = flightRepository.getCachedFlights()
                    _uiState.value = _uiState.value.copy(
                        flights = cached,
                        isFlightsLoading = false,
                        flightsError = if (cached.isEmpty()) error.message else null,
                        flightsLastUpdated = cacheManager.getLastUpdateTime("flights")
                    )
                }
            } catch (e: Exception) {
                val cached = flightRepository.getCachedFlights()
                _uiState.value = _uiState.value.copy(
                    flights = cached,
                    isFlightsLoading = false,
                    flightsError = e.message,
                    flightsLastUpdated = cacheManager.getLastUpdateTime("flights")
                )
            }
        }
    }

    // ==================== SPOTIFY ====================

    private fun loadSpotify() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSpotifyLoading = true)
            
            try {
                val result = spotifyRepository.fetchAndCachePlayback()
                result.onSuccess { track ->
                    _uiState.value = _uiState.value.copy(
                        spotifyTrack = track,
                        isSpotifyLoading = false,
                        spotifyError = null,
                        spotifyLastUpdated = System.currentTimeMillis()
                    )
                    updateMascotMood()
                }.onFailure { error ->
                    val cached = spotifyRepository.getCachedTrack()
                    _uiState.value = _uiState.value.copy(
                        spotifyTrack = cached,
                        isSpotifyLoading = false,
                        spotifyError = if (cached == null || !cached.isPlaying) null else error.message,
                        spotifyLastUpdated = cacheManager.getLastUpdateTime("spotify")
                    )
                }
            } catch (e: Exception) {
                val cached = spotifyRepository.getCachedTrack()
                _uiState.value = _uiState.value.copy(
                    spotifyTrack = cached,
                    isSpotifyLoading = false,
                    spotifyError = e.message,
                    spotifyLastUpdated = cacheManager.getLastUpdateTime("spotify")
                )
            }
        }
    }

    // ==================== TRACKED ITEMS ====================

    /** Flight numbers the user has chosen to track (Bible §5). */
    val trackedFlights: List<String>
        get() = preferencesManager.getTrackedFlights()

    /** RSS/Atom feed URLs the user has configured. */
    val rssFeeds: List<String>
        get() = preferencesManager.getRssFeeds()

    fun addTrackedFlight(flightNumber: String) {
        viewModelScope.launch {
            val current = preferencesManager.getTrackedFlights()
            if (flightNumber.isBlank() || current.any { it.equals(flightNumber, true) }) return@launch
            // Bible §2E tracks 1-2 flights; keep the most recent two so the free-tier
            // request budget stays predictable.
            preferencesManager.saveTrackedFlights((current + flightNumber).takeLast(2))
            loadFlights()
        }
    }

    fun removeTrackedFlight(flightNumber: String) {
        viewModelScope.launch {
            preferencesManager.saveTrackedFlights(
                preferencesManager.getTrackedFlights().filterNot { it.equals(flightNumber, true) }
            )
            loadFlights()
        }
    }

    fun addRssFeed(url: String) {
        viewModelScope.launch {
            val current = preferencesManager.getRssFeeds()
            if (url.isBlank() || url in current) return@launch
            preferencesManager.saveRssFeeds(current + url)
            loadNews()
        }
    }

    fun removeRssFeed(url: String) {
        viewModelScope.launch {
            preferencesManager.saveRssFeeds(preferencesManager.getRssFeeds() - url)
            loadNews()
        }
    }

    /** Re-reads the calendar after the user grants permission. */
    fun onCalendarPermissionGranted() {
        loadCalendar()
    }

    // ==================== WIDGET LAYOUT ====================

    fun toggleWidget(widget: DashboardWidget) {
        val next = _uiState.value.widgetLayout.toggled(widget)
        preferencesManager.saveWidgetLayout(next)
        _uiState.value = _uiState.value.copy(widgetLayout = next)
    }

    fun moveWidget(widget: DashboardWidget, up: Boolean) {
        val next = _uiState.value.widgetLayout.moved(widget, up)
        preferencesManager.saveWidgetLayout(next)
        _uiState.value = _uiState.value.copy(widgetLayout = next)
    }

    // ==================== CALENDAR ====================

    private fun loadCalendar() {
        viewModelScope.launch {
            // Absence of permission is a distinct state from an error: the widget should
            // offer a "grant access" action rather than a "retry" that can never succeed.
            if (!calendarRepository.hasPermission()) {
                _uiState.value = _uiState.value.copy(
                    isCalendarLoading = false,
                    calendarPermissionMissing = true,
                    calendarError = null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isCalendarLoading = true,
                calendarPermissionMissing = false
            )

            calendarRepository.fetchAndCacheEvents()
                .onSuccess { events ->
                    _uiState.value = _uiState.value.copy(
                        calendarEvents = events,
                        isCalendarLoading = false,
                        calendarError = null,
                        calendarLastUpdated = System.currentTimeMillis()
                    )
                    updateMascotMood()
                }
                .onFailure { error ->
                    val cached = calendarRepository.getCachedEvents()
                    _uiState.value = _uiState.value.copy(
                        calendarEvents = cached,
                        isCalendarLoading = false,
                        calendarError = if (cached.isEmpty()) error.message else null
                    )
                }
        }
    }

    // ==================== MASCOT ====================

    private fun loadMascot() {
        viewModelScope.launch {
            try {
                val mascotState = mascotRepository.getCachedMascotState()
                _uiState.value = _uiState.value.copy(
                    mascotState = mascotState,
                    mascotAnimationsEnabled = preferencesManager.areMascotAnimationsEnabled(),
                    darkMode = preferencesManager.getDarkMode()
                )
            } catch (e: Exception) {
                // Use default mascot state on error
                _uiState.value = _uiState.value.copy(
                    mascotState = MascotStateEntity(
                        character = "robot",
                        mood = "neutral",
                        animation = "robot_idle",
                        lastUpdate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    /**
     * Update mascot mood based on current dashboard data
     */
    private fun updateMascotMood() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newMood = calculateMascotMood(currentState)
            
            val currentMascot = currentState.mascotState ?: MascotStateEntity(
                character = "robot",
                mood = "neutral",
                animation = "robot_idle",
                lastUpdate = System.currentTimeMillis()
            )
            
            if (currentMascot.mood != newMood) {
                val updatedMascot = currentMascot.copy(
                    mood = newMood,
                    lastUpdate = System.currentTimeMillis()
                )
                mascotRepository.updateMascotState(updatedMascot)
                _uiState.value = currentState.copy(mascotState = updatedMascot)
            }
        }
    }

    /**
     * Calculate mascot mood based on various factors
     */
    private fun calculateMascotMood(state: DashboardUiState): String {
        // Check for concerning conditions
        val hasFlightIssues = state.flights.any { 
            it.status.lowercase().contains("delayed") || 
            it.status.lowercase().contains("cancelled") 
        }
        
        val hasBadWeather = state.weather?.let { weather ->
            weather.condition.lowercase() in listOf("storm", "thunderstorm", "heavy rain", "snow")
        } ?: false
        
        val hasErrors = listOf(
            state.weatherError,
            state.newsError,
            state.flightsError,
            state.spotifyError
        ).any { it != null }
        
        // Check for exciting conditions
        val isPlayingMusic = state.spotifyTrack?.isPlaying == true
        val hasGoodWeather = state.weather?.let { weather ->
            weather.condition.lowercase() in listOf("clear", "sunny", "partly cloudy")
        } ?: false
        
        // Determine mood
        return when {
            hasFlightIssues || hasBadWeather || hasErrors -> "concerned"
            isPlayingMusic && hasGoodWeather -> "excited"
            isPlayingMusic || hasGoodWeather -> "happy"
            else -> "neutral"
        }
    }

    // ==================== CACHE STATUS ====================

    private fun observeCacheStatus() {
        viewModelScope.launch {
            cacheManager.globalCacheStatus.collect { statusMap ->
                _cacheStatus.value = statusMap
            }
        }
    }

    /**
     * Get human-readable time since last update for a widget
     */
    fun getTimeSinceUpdate(widgetType: WidgetType): String {
        val timestamp = when (widgetType) {
            WidgetType.WEATHER -> _uiState.value.weatherLastUpdated
            WidgetType.NEWS -> _uiState.value.newsLastUpdated
            WidgetType.FLIGHTS -> _uiState.value.flightsLastUpdated
            WidgetType.SPOTIFY -> _uiState.value.spotifyLastUpdated
            WidgetType.CALENDAR -> _uiState.value.calendarLastUpdated
            WidgetType.MASCOT -> _uiState.value.mascotState?.lastUpdate
        }
        
        return timestamp?.let { cacheManager.getHumanReadableTime(it) } ?: "Never"
    }

    /**
     * Check if a specific widget's data is stale
     */
    fun isWidgetStale(widgetType: WidgetType): Boolean {
        val cacheInfo = when (widgetType) {
            WidgetType.WEATHER -> _cacheStatus.value["weather"]
            WidgetType.CALENDAR -> _cacheStatus.value["calendar"]
            WidgetType.NEWS -> _cacheStatus.value["news"]
            WidgetType.FLIGHTS -> _cacheStatus.value["flights"]
            WidgetType.SPOTIFY -> _cacheStatus.value["spotify"]
            WidgetType.MASCOT -> null
        }
        
        return cacheInfo?.level in listOf(
            CacheLevel.STALE,
            CacheLevel.EXPIRED,
            CacheLevel.VERY_OLD
        )
    }
}

/**
 * Widget types for targeted refresh operations
 */
enum class WidgetType {
    WEATHER,
    CALENDAR,
    NEWS,
    FLIGHTS,
    SPOTIFY,
    MASCOT
}

