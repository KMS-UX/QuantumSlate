package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.ui.permissions.rememberCalendarPermissionRequester
import com.quantumslate.dashboard.data.local.CacheLevel
import com.quantumslate.dashboard.data.local.cacheLevelFor
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RetroNewspaperDashboard(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    darkMode: PreferencesManager.DarkMode = PreferencesManager.DarkMode.AUTO
) {
    QuantumSlateTheme(
        uiMode = PreferencesManager.UiMode.RETRO,
        darkMode = darkMode
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Thin rule directly under the masthead, as on a period front page.
                        Divider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth(0.72f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Folio line: edition marker, date, price - the newspaper convention.
                        Row(
                            modifier = Modifier.fillMaxWidth(0.72f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "VOL. I",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                            )
                            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date()).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "PRICE 5¢",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // Gear sits in the masthead's top-right corner (Bible §3).
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Column {
                    Divider(
                        thickness = 3.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

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
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "High: ${weather.highTemp.toInt()}° | Low: ${weather.lowTemp.toInt()}°",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                } ?: run {
                                    Text(
                                        text = "Forecast unavailable",
                                        style = MaterialTheme.typography.bodyMedium,
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

            val requestCalendarPermission = rememberCalendarPermissionRequester { granted ->
                if (granted) dashboardViewModel.onCalendarPermissionGranted()
            }
                        
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
                            MascotWidget(
                                mascotState = uiState.mascotState,
                                size = 120,
                                animationsEnabled = uiState.mascotAnimationsEnabled
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
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Rendered in the newspaper's own voice rather than with the
                                // Material calendar card, to keep the retro column intact.
                                when {
                                    uiState.calendarPermissionMissing -> {
                                        Text(
                                            text = "Calendar access required",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tap to grant",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable { requestCalendarPermission() }
                                        )
                                    }

                                    uiState.calendarEvents.isEmpty() -> Text(
                                        text = "No events scheduled",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )

                                    else -> uiState.calendarEvents.take(3).forEach { event ->
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = retroEventTime(event.startTime, event.isAllDay),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
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
                                        cacheLevelFor(it, 2 * 60 * 60 * 1000) 
                                    } ?: CacheLevel.EXPIRED
                                )
                            }
                            
                            IconButton(onClick = { dashboardViewModel.refreshWidget(WidgetType.NEWS) }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
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
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else if (uiState.newsError != null && uiState.newsArticles.isEmpty()) {
                        Text(
                            text = "News unavailable: ${uiState.newsError}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (uiState.newsArticles.isEmpty()) {
                        Text(
                            text = "No headlines available. Add RSS feeds in settings.",
                            style = MaterialTheme.typography.bodyMedium,
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
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                    article.pubDate.let { published ->
                                        Text(
                                            text = formatRelativeTime(published),
                                            style = MaterialTheme.typography.labelSmall,
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

/** Event time in the newspaper's register, e.g. "Today, 14:30" or "Fri 12 Sep — all day". */
private fun retroEventTime(startTime: Long, isAllDay: Boolean): String {
    val start = Date(startTime)
    val today = SimpleDateFormat("yyyyDDD", Locale.getDefault())
    val isToday = today.format(start) == today.format(Date())
    val dayLabel = if (isToday) {
        "Today"
    } else {
        SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(start)
    }
    return if (isAllDay) {
        "$dayLabel — all day"
    } else {
        "$dayLabel, " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(start)
    }
}
