package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val weatherViewModel: WeatherViewModel = hiltViewModel()
                val weatherState by weatherViewModel.weatherState.collectAsState()

                // Large time and date display
                TimeDateWidget(
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Weather widget below
                WeatherWidget(
                    weather = weatherState.weather,
                    isLoading = weatherState.isLoading,
                    errorMessage = weatherState.errorMessage
                )
            }
        }
    }
}
