package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.ui.permissions.rememberCalendarPermissionRequester
import com.quantumslate.dashboard.data.local.CacheLevel
import com.quantumslate.dashboard.data.local.cacheLevelFor
import com.quantumslate.dashboard.data.local.DashboardWidget
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.domain.model.CalendarEvent
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataDenseDashboard(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    darkMode: PreferencesManager.DarkMode = PreferencesManager.DarkMode.AUTO
) {
    QuantumSlateTheme(
        uiMode = PreferencesManager.UiMode.DATA_DENSE,
        darkMode = darkMode
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val uiState by dashboardViewModel.uiState.collectAsState()

            var showAddFlight by remember { mutableStateOf(false) }
            var showAddFeed by remember { mutableStateOf(false) }
            val requestCalendarPermission = rememberCalendarPermissionRequester { granted ->
                if (granted) dashboardViewModel.onCalendarPermissionGranted()
            }

            if (showAddFlight) {
                AddFlightDialog(
                    onDismiss = { showAddFlight = false },
                    onConfirm = { flight ->
                        dashboardViewModel.addTrackedFlight(flight)
                        showAddFlight = false
                    },
                    existingFlights = dashboardViewModel.trackedFlights
                )
            }

            if (showAddFeed) {
                AddFeedDialog(
                    onDismiss = { showAddFeed = false },
                    onConfirm = { url ->
                        dashboardViewModel.addRssFeed(url)
                        showAddFeed = false
                    },
                    existingFeeds = dashboardViewModel.rssFeeds
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row with time and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeDateWidgetCompact()
                    
                    // Global refresh button
                    if (uiState.widgetLayout.isEnabled(DashboardWidget.MASCOT)) {
                        MascotWidget(
                            mascotState = uiState.mascotState,
                            size = 72,
                            animationsEnabled = uiState.mascotAnimationsEnabled
                        )
                    }

                    IconButton(onClick = { dashboardViewModel.refreshAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh All",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Grid of widgets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Weather widget with cache status
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        WeatherWidgetWithStatus(
                            weather = uiState.weather,
                            isLoading = uiState.isWeatherLoading,
                            errorMessage = uiState.weatherError,
                            lastUpdated = uiState.weatherLastUpdated,
                            cacheLevel = uiState.weatherLastUpdated?.let { 
                                cacheLevelFor(it, 30 * 60 * 1000) 
                            } ?: CacheLevel.EXPIRED,
                            onRefresh = { dashboardViewModel.refreshWidget(WidgetType.WEATHER) },
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    // Calendar widget
                    if (uiState.widgetLayout.isEnabled(DashboardWidget.CALENDAR)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            CalendarWidgetWithStatus(
                                events = uiState.calendarEvents,
                                isLoading = uiState.isCalendarLoading,
                                errorMessage = uiState.calendarError,
                                permissionMissing = uiState.calendarPermissionMissing,
                                lastUpdated = uiState.calendarLastUpdated,
                                cacheLevel = uiState.calendarLastUpdated?.let {
                                    cacheLevelFor(it, 15 * 60 * 1000)
                                } ?: CacheLevel.EXPIRED,
                                onRefresh = { dashboardViewModel.refreshWidget(WidgetType.CALENDAR) },
                                onRequestPermission = requestCalendarPermission,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
                
                // News widget
                if (uiState.widgetLayout.isEnabled(DashboardWidget.NEWS)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        NewsWidgetWithStatus(
                            articles = uiState.newsArticles,
                            isLoading = uiState.isNewsLoading,
                            errorMessage = uiState.newsError,
                            lastUpdated = uiState.newsLastUpdated,
                            cacheLevel = uiState.newsLastUpdated?.let { 
                                cacheLevelFor(it, 2 * 60 * 60 * 1000) 
                            } ?: CacheLevel.EXPIRED,
                            onRefresh = { dashboardViewModel.refreshWidget(WidgetType.NEWS) },
                            onArticleClick = { url -> /* Open URL */ },
                            onAddFeed = { showAddFeed = true },
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                
                // Flight status widget
                if (uiState.widgetLayout.isEnabled(DashboardWidget.FLIGHTS)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        FlightWidgetWithStatus(
                            flights = uiState.flights,
                            isLoading = uiState.isFlightsLoading,
                            errorMessage = uiState.flightsError,
                            lastUpdated = uiState.flightsLastUpdated,
                            cacheLevel = uiState.flightsLastUpdated?.let { 
                                cacheLevelFor(it, 5 * 60 * 1000) 
                            } ?: CacheLevel.EXPIRED,
                            onRefresh = { dashboardViewModel.refreshWidget(WidgetType.FLIGHTS) },
                            onAddFlight = { showAddFlight = true },
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                
                // Spotify widget
                if (uiState.widgetLayout.isEnabled(DashboardWidget.SPOTIFY)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        SpotifyWidgetWithStatus(
                            track = uiState.spotifyTrack,
                            isLoading = uiState.isSpotifyLoading,
                            errorMessage = uiState.spotifyError,
                            lastUpdated = uiState.spotifyLastUpdated,
                            cacheLevel = uiState.spotifyLastUpdated?.let { 
                                cacheLevelFor(it, 30 * 1000) 
                            } ?: CacheLevel.EXPIRED,
                            onRefresh = { dashboardViewModel.refreshWidget(WidgetType.SPOTIFY) },
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDateWidgetCompact() {
    val currentTime = System.currentTimeMillis()
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())

    Column {
        Text(
            text = timeFormat.format(Date(currentTime)),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = dateFormat.format(Date(currentTime)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CalendarEventItem(event: CalendarEvent) {
    val timeFormat = if (event.isAllDay) "All day" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.startTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Surface(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp),
            color = Color(event.color)
        ) {}

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = timeFormat,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
