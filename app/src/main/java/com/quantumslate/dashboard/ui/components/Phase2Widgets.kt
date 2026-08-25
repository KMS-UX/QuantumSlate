package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.quantumslate.dashboard.data.local.NewsArticleEntity
import com.quantumslate.dashboard.data.local.FlightEntity
import com.quantumslate.dashboard.data.local.SpotifyTrackEntity
import com.quantumslate.dashboard.domain.model.MascotMood
import java.text.SimpleDateFormat
import java.util.*

// ==================== RSS NEWS WIDGET ====================

@Composable
fun NewsWidget(
    articles: List<NewsArticleEntity>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onArticleClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📰 News Headlines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (articles.isEmpty()) {
                Text(
                    text = "No news articles. Add RSS feeds in settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                LazyColumn {
                    items(articles.take(5)) { article ->
                        NewsItem(article, onArticleClick)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsItem(
    article: NewsArticleEntity,
    onArticleClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onArticleClick(article.link) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = article.source,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== FLIGHT STATUS WIDGET ====================

@Composable
fun FlightStatusWidget(
    flights: List<FlightEntity>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onAddFlight: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                
                IconButton(onClick = onAddFlight) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Add Flight",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (flights.isEmpty()) {
                Text(
                    text = "No tracked flights. Tap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                flights.forEach { flight ->
                    FlightItem(flight)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FlightItem(flight: FlightEntity) {
    val statusColor = when {
        flight.status.lowercase().contains("delayed") || flight.status.lowercase().contains("cancelled") -> MaterialTheme.colorScheme.error
        flight.status.lowercase().contains("on time") || flight.status.lowercase().contains("landed") -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
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

// ==================== SPOTIFY NOW PLAYING WIDGET ====================

@Composable
fun SpotifyWidget(
    track: SpotifyTrackEntity?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1DB954).copy(alpha = 0.9f))
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
                    imageVector = androidx.compose.material.icons.Icons.Default.MusicNote,
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
                    AsyncImage(
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
                            imageVector = androidx.compose.material.icons.Icons.Default.MusicNote,
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
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = track.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                    contentDescription = "Playing",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}

// ==================== VIRTUAL MASCOT WIDGET ====================

@Composable
fun MascotWidget(
    mascotState: com.quantumslate.dashboard.data.local.MascotStateEntity?,
    modifier: Modifier = Modifier,
    size: Float = 100f
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mascotState == null) {
            // Default mascot
            Text(
                text = "🤖",
                style = MaterialTheme.typography.displayMedium
            )
        } else {
            // Display mascot based on character and mood
            val mascotChar = when (mascotState.character.lowercase()) {
                "cat" -> "🐱"
                "bird" -> "🐦"
                "creature" -> "👾"
                else -> "🤖" // robot default
            }
            
            val accessory = when (mascotState.mood.lowercase()) {
                "happy" -> "😊"
                "excited" -> "🎉"
                "concerned" -> "😟"
                "sleepy" -> "💤"
                else -> ""
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mascotChar,
                    style = MaterialTheme.typography.displayMedium
                )
                if (accessory.isNotEmpty()) {
                    Text(
                        text = accessory,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}

// ==================== COMBINED PHASE 2 WIDGETS CONTAINER ====================

@Composable
fun Phase2WidgetsContainer(
    newsArticles: List<NewsArticleEntity> = emptyList(),
    flights: List<FlightEntity> = emptyList(),
    spotifyTrack: SpotifyTrackEntity? = null,
    mascotState: com.quantumslate.dashboard.data.local.MascotStateEntity? = null,
    isLoadingNews: Boolean = false,
    isLoadingFlights: Boolean = false,
    isLoadingSpotify: Boolean = false,
    newsError: String? = null,
    onArticleClick: (String) -> Unit = {},
    onAddFlight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NewsWidget(
            articles = newsArticles,
            isLoading = isLoadingNews,
            errorMessage = newsError,
            onArticleClick = onArticleClick,
            modifier = Modifier.fillMaxWidth()
        )
        
        FlightStatusWidget(
            flights = flights,
            isLoading = isLoadingFlights,
            onAddFlight = onAddFlight,
            modifier = Modifier.fillMaxWidth()
        )
        
        SpotifyWidget(
            track = spotifyTrack,
            isLoading = isLoadingSpotify,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            MascotWidget(
                mascotState = mascotState,
                size = 80f
            )
        }
    }
}
