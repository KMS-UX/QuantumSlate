package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.ui.permissions.rememberCalendarPermissionRequester
import com.quantumslate.dashboard.data.local.CacheLevel
import com.quantumslate.dashboard.data.local.cacheLevelFor
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme

@Composable
fun MinimalistDashboard(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    darkMode: PreferencesManager.DarkMode = PreferencesManager.DarkMode.AUTO
) {
    QuantumSlateTheme(
        uiMode = PreferencesManager.UiMode.MINIMALIST,
        darkMode = darkMode
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val uiState by dashboardViewModel.uiState.collectAsState()

            val requestCalendarPermission = rememberCalendarPermissionRequester { granted ->
                if (granted) dashboardViewModel.onCalendarPermissionGranted()
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 56.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large time and date display with refresh
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeDateWidget(
                        modifier = Modifier.weight(1f)
                    )

                    MascotWidget(
                        mascotState = uiState.mascotState,
                        size = 96,
                        animationsEnabled = uiState.mascotAnimationsEnabled
                    )
                    
                    IconButton(onClick = { dashboardViewModel.refreshAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
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

                Spacer(modifier = Modifier.height(56.dp))

                // Weather widget with cache status
                WeatherWidgetWithStatus(
                    weather = uiState.weather,
                    isLoading = uiState.isWeatherLoading,
                    errorMessage = uiState.weatherError,
                    lastUpdated = uiState.weatherLastUpdated,
                    cacheLevel = uiState.weatherLastUpdated?.let { 
                        cacheLevelFor(it, 30 * 60 * 1000) 
                    } ?: CacheLevel.EXPIRED,
                    onRefresh = { dashboardViewModel.refreshWidget(WidgetType.WEATHER) }
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                    // Mode A shows only what matters at a glance.
                    maxEvents = 2
                )
            }
        }
    }
}
