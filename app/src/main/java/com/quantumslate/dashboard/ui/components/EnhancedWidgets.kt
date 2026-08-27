package com.quantumslate.dashboard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import com.quantumslate.dashboard.data.local.CacheLevel
import com.quantumslate.dashboard.data.local.CacheManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced widget wrappers that integrate cache status and error handling
 */

// ==================== WEATHER WIDGET WITH CACHE STATUS ====================

@Composable
fun WeatherWidgetWithStatus(
    weather: com.quantumslate.dashboard.domain.model.Weather?,
    isLoading: Boolean,
    errorMessage: String?,
    lastUpdated: Long?,
    cacheLevel: CacheLevel,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    /** Supplied when weather is blocked purely on the location permission. */
    onRequestLocation: (() -> Unit)? = null
) {
    if (onRequestLocation != null && weather == null) {
        PermissionPromptCard(
            title = "🌤️ Weather",
            message = errorMessage ?: "QuantumSlate needs location access to show local weather.",
            actionLabel = "Grant access",
            onAction = onRequestLocation,
            modifier = modifier
        )
        return
    }

    WidgetStateHandler(
        isLoading = isLoading,
        hasError = errorMessage != null,
        isEmpty = weather == null,
        errorMessage = errorMessage,
        loadingMessage = "Fetching weather...",
        emptyMessage = "No weather data available",
        onRetry = onRefresh,
        onRefresh = onRefresh
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with refresh button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌤️ Weather",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (lastUpdated != null) {
                            StaleDataIndicator(
                                lastUpdated = lastUpdated,
                                cacheLevel = cacheLevel
                            )
                        }
                        
                        IconButton(
                            onClick = onRefresh,
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Weather content
                weather?.let { w ->
                    Text(
                        text = "${w.temperature.toInt()}°C",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = w.condition.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "H: ${w.highTemp.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "L: ${w.lowTemp.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    
                    Text(
                        text = "📍 ${w.location}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ==================== NEWS WIDGET WITH CACHE STATUS ====================

@Composable
fun NewsWidgetWithStatus(
    articles: List<com.quantumslate.dashboard.data.local.NewsArticleEntity>,
    isLoading: Boolean,
    errorMessage: String?,
    lastUpdated: Long?,
    cacheLevel: CacheLevel,
    onRefresh: () -> Unit,
    onArticleClick: (String) -> Unit,
    onAddFeed: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    WidgetStateHandler(
        isLoading = isLoading,
        hasError = errorMessage != null && articles.isEmpty(),
        isEmpty = articles.isEmpty() && errorMessage == null,
        errorMessage = errorMessage,
        loadingMessage = "Fetching news...",
        emptyMessage = "No news articles yet. Add an RSS feed to get started.",
        onRetry = onRefresh,
        onRefresh = onRefresh,
        onAddData = onAddFeed
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📰 News Headlines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (lastUpdated != null) {
                            StaleDataIndicator(
                                lastUpdated = lastUpdated,
                                cacheLevel = cacheLevel
                            )
                        }
                        
                        if (onAddFeed != null) {
                            IconButton(onClick = onAddFeed) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add news feed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefresh,
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // News list
                if (articles.isNotEmpty()) {
                    articles.take(5).forEach { article ->
                        NewsItemEnhanced(article, onArticleClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsItemEnhanced(
    article: com.quantumslate.dashboard.data.local.NewsArticleEntity,
    onArticleClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onArticleClick(article.link) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                article.pubDate.let { published ->
                    Text(
                        text = formatRelativeTime(published),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ==================== FLIGHT WIDGET WITH CACHE STATUS ====================

@Composable
fun FlightWidgetWithStatus(
    flights: List<com.quantumslate.dashboard.data.local.FlightEntity>,
    isLoading: Boolean,
    errorMessage: String?,
    lastUpdated: Long?,
    cacheLevel: CacheLevel,
    onRefresh: () -> Unit,
    onAddFlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    WidgetStateHandler(
        isLoading = isLoading,
        hasError = errorMessage != null && flights.isEmpty(),
        isEmpty = flights.isEmpty() && errorMessage == null,
        errorMessage = errorMessage,
        loadingMessage = "Fetching flight status...",
        emptyMessage = "No tracked flights yet.",
        onRetry = onRefresh,
        onRefresh = onRefresh,
        onAddData = onAddFlight
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✈️ Flight Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (lastUpdated != null) {
                            StaleDataIndicator(
                                lastUpdated = lastUpdated,
                                cacheLevel = cacheLevel
                            )
                        }
                        
                        IconButton(
                            onClick = onAddFlight
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Flight",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onRefresh,
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Flight list
                if (flights.isNotEmpty()) {
                    flights.forEach { flight ->
                        FlightItemEnhanced(flight)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightItemEnhanced(flight: com.quantumslate.dashboard.data.local.FlightEntity) {
    val statusColor = when {
        flight.status.lowercase().contains("delayed") || 
        flight.status.lowercase().contains("cancelled") -> MaterialTheme.colorScheme.error
        flight.status.lowercase().contains("on time") || 
        flight.status.lowercase().contains("landed") -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = flight.flightNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = flight.status,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = flight.departureAirport,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = flight.arrivalAirport,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (!flight.gate.isNullOrBlank()) {
                    Text(
                        text = "Gate: ${flight.gate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==================== SPOTIFY WIDGET WITH CACHE STATUS ====================

@Composable
fun SpotifyWidgetWithStatus(
    track: com.quantumslate.dashboard.data.local.SpotifyTrackEntity?,
    isLoading: Boolean,
    errorMessage: String?,
    lastUpdated: Long?,
    cacheLevel: CacheLevel,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1DB954).copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            } else if (track == null || !track.isPlaying) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Not Playing",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Open Spotify to start listening",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            } else {
                // Album art
                if (!track.albumArtUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = track.albumArtUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.trackName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = track.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatDuration(track.progressMs) + " / " + formatDuration(track.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Playing indicator
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Playing",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Refresh button
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Cache status indicator at bottom
        if (lastUpdated != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                StaleDataIndicator(
                    lastUpdated = lastUpdated,
                    cacheLevel = cacheLevel,
                    textColor = Color.White
                )
            }
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

@Composable
internal fun StaleDataIndicator(
    lastUpdated: Long,
    cacheLevel: CacheLevel,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val timeAgo = getHumanReadableTimeAgo(lastUpdated)
    val indicatorColor = when (cacheLevel) {
        CacheLevel.FRESH -> Color(0xFF4CAF50)      // Green
        CacheLevel.STALE -> Color(0xFFFFB74D)      // Amber
        CacheLevel.EXPIRED -> Color(0xFFFF9800)    // Orange
        CacheLevel.VERY_OLD -> Color(0xFFF44336)   // Red
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        
        Text(
            text = timeAgo,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

private fun getHumanReadableTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 5 * 60 * 1000 -> "${diff / 60000}m ago"
        diff < 60 * 60 * 1000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}

internal fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Now"
        diff < 60 * 60 * 1000 -> "${diff / 3600000}h ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / 86400000}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

/**
 * Shared prompt for a widget that is blocked on a runtime permission.
 *
 * Distinct from an error state on purpose: a "retry" button can never succeed without the
 * grant, so offering one would just loop the user.
 */
@Composable
internal fun PermissionPromptCard(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
