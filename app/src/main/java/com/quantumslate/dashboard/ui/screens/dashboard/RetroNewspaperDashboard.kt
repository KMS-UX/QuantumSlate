package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RetroNewspaperDashboard(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {}
) {
    QuantumSlateTheme(
        uiMode = PreferencesManager.UiMode.RETRO
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Newspaper header
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "The Daily Quantum",
                            style = MaterialTheme.typography.displayLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )

                // Main content area with columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left column - Time and Weather
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Time box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            Text(
                                text = timeFormat.format(Date()),
                                style = MaterialTheme.typography.headlineLarge,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Weather forecast box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Weather Forecast",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val weatherViewModel: WeatherViewModel = hiltViewModel()
                                val weatherState by weatherViewModel.weatherState.collectAsState()
                                
                                weatherState.weather?.let { weather ->
                                    Text(
                                        text = "${weather.temperature.toInt()}°C - ${weather.condition}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "High: ${weather.highTemp.toInt()}° | Low: ${weather.lowTemp.toInt()}°",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                } ?: run {
                                    Text(
                                        text = "Forecast unavailable",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    // Right column - Mascot and Social Calendar
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mascot section
                        val dashboardViewModel: DashboardViewModel = hiltViewModel()
                        val uiState by dashboardViewModel.uiState.collectAsState()
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieMascotWidget(
                                mascotState = uiState.mascotState ?: com.quantumslate.dashboard.data.local.MascotStateEntity(
                                    character = "robot",
                                    mood = "neutral",
                                    lastUpdated = System.currentTimeMillis()
                                ),
                                size = 100f
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Social Calendar box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Social Calendar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No events scheduled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                )

                // Bottom section - Headlines from RSS News
                Column {
                    val dashboardViewModel: DashboardViewModel = hiltViewModel()
                    val uiState by dashboardViewModel.uiState.collectAsState()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latest Headlines",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        // Refresh button and cache status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.newsLastUpdated != null) {
                                StaleDataIndicator(
                                    lastUpdated = uiState.newsLastUpdated!!,
                                    cacheLevel = uiState.newsLastUpdated?.let { 
                                        com.quantumslate.dashboard.data.local.CacheManager.getCacheLevel(it, 2 * 60 * 60 * 1000) 
                                    } ?: com.quantumslate.dashboard.data.local.CacheManager.CacheLevel.EXPIRED
                                )
                            }
                            
                            IconButton(onClick = { dashboardViewModel.refreshWidget(WidgetType.NEWS) }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                                    contentDescription = "Refresh News",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = androidx.compose.ui.Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.isNewsLoading) {
                        Text(
                            text = "Loading headlines...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else if (uiState.newsError != null && uiState.newsArticles.isEmpty()) {
                        Text(
                            text = "News unavailable: ${uiState.newsError}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (uiState.newsArticles.isEmpty()) {
                        Text(
                            text = "No headlines available. Add RSS feeds in settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        // Display up to 3 headlines
                        uiState.newsArticles.take(3).forEach { article ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "• ${article.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = article.source,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                    article.publishedAt?.let { published ->
                                        Text(
                                            text = com.quantumslate.dashboard.data.local.CacheManager.getHumanReadableTime(published),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Serif,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
