package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quantumslate.dashboard.data.local.CacheAgeDisplay
import com.quantumslate.dashboard.data.local.CacheLevel

/**
 * Reusable error state widget for displaying errors with retry functionality.
 */
@Composable
fun ErrorStateWidget(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Unable to Load Data"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

/**
 * Displays a warning indicator for stale cached data.
 */
@Composable
fun StaleDataIndicator(
    cacheAgeDisplay: CacheAgeDisplay,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    if (!cacheAgeDisplay.showWarning) return
    
    val iconColor = when (cacheAgeDisplay.level) {
        CacheLevel.FRESH -> Color.Transparent // Shouldn't show
        CacheLevel.STALE -> Color(0xFFFFA000) // Amber warning
        CacheLevel.EXPIRED -> Color(0xFFFF5722) // Orange error
        CacheLevel.VERY_OLD -> Color(0xFFF44336) // Red critical
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Stale data warning",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        
        Text(
            text = "Updated ${cacheAgeDisplay.text}",
            style = MaterialTheme.typography.labelSmall,
            color = iconColor
        )
        
        if (cacheAgeDisplay.shouldRefresh && onRefresh != null) {
            Button(
                onClick = onRefresh,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Compact loading indicator for widgets.
 */
@Composable
fun WidgetLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Empty state widget for when there's no data to display.
 */
@Composable
fun EmptyStateWidget(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * Combined widget state handler that displays appropriate UI based on state.
 */
@Composable
fun WidgetStateHandler(
    isLoading: Boolean,
    hasError: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
    emptyMessage: String = "No data available",
    cacheAgeDisplay: CacheAgeDisplay? = null,
    onRetry: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    onAddData: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> {
            WidgetLoadingIndicator()
        }
        hasError && errorMessage != null -> {
            ErrorStateWidget(
                errorMessage = errorMessage,
                onRetry = onRetry
            )
        }
        isEmpty -> {
            EmptyStateWidget(
                message = emptyMessage,
                actionLabel = if (onAddData != null) "Add" else null,
                onAction = onAddData
            )
        }
        else -> {
            Column {
                content()
                
                // Show cache age indicator if available
                if (cacheAgeDisplay != null && cacheAgeDisplay.showWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StaleDataIndicator(
                        cacheAgeDisplay = cacheAgeDisplay,
                        onRefresh = onRefresh
                    )
                }
            }
        }
    }
}
