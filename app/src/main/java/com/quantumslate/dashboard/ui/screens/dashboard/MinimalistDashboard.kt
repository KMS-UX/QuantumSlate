package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme

@Composable
fun MinimalistDashboard(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {}
) {
    QuantumSlateTheme(
        uiMode = PreferencesManager.UiMode.MINIMALIST
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val uiState by dashboardViewModel.uiState.collectAsState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
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
                    
                    IconButton(onClick = { dashboardViewModel.refreshAll() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Weather widget with cache status
                WeatherWidgetWithStatus(
                    weather = uiState.weather,
                    isLoading = uiState.isWeatherLoading,
                    errorMessage = uiState.weatherError,
                    lastUpdated = uiState.weatherLastUpdated,
                    cacheLevel = uiState.weatherLastUpdated?.let { 
                        com.quantumslate.dashboard.data.local.CacheManager.getCacheLevel(it, 30 * 60 * 1000) 
                    } ?: com.quantumslate.dashboard.data.local.CacheManager.CacheLevel.EXPIRED,
                    onRefresh = { dashboardViewModel.refreshWidget(WidgetType.WEATHER) }
                )
            }
        }
    }
}
