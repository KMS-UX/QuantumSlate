package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quantumslate.dashboard.data.local.CacheLevel
import com.quantumslate.dashboard.data.local.CalendarEventEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Calendar widget: the next few upcoming events, colour-coded by source calendar.
 *
 * Bible §2C. Handles the permission-missing case separately from a data error, because a
 * "retry" button can never succeed without the grant.
 */
@Composable
fun CalendarWidgetWithStatus(
    events: List<CalendarEventEntity>,
    isLoading: Boolean,
    errorMessage: String?,
    permissionMissing: Boolean,
    lastUpdated: Long?,
    cacheLevel: CacheLevel,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    maxEvents: Int = 3
) {
    if (permissionMissing) {
        CalendarPermissionCard(
            onRequestPermission = onRequestPermission,
            modifier = modifier
        )
        return
    }

    WidgetStateHandler(
        isLoading = isLoading,
        hasError = errorMessage != null && events.isEmpty(),
        isEmpty = events.isEmpty() && errorMessage == null,
        errorMessage = errorMessage,
        loadingMessage = "Reading calendar...",
        emptyMessage = "No upcoming events in the next 7 days.",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Calendar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (lastUpdated != null) {
                            StaleDataIndicator(
                                lastUpdated = lastUpdated,
                                cacheLevel = cacheLevel
                            )
                        }
                        IconButton(onClick = onRefresh, enabled = !isLoading) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh calendar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                events.take(maxEvents).forEach { event ->
                    CalendarEventRow(event)
                }
            }
        }
    }
}

@Composable
private fun CalendarEventRow(event: CalendarEventEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Colour stripe identifies which calendar the event came from (Bible §2C).
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(calendarColor(event.color))
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatEventTime(event),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalendarPermissionCard(
    onRequestPermission: () -> Unit,
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
                text = "📅 Calendar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "QuantumSlate needs permission to read your calendar to show upcoming events.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Button(onClick = onRequestPermission) {
                Text("Grant access")
            }
        }
    }
}

/**
 * The provider stores calendar colour as a packed ARGB int; a 0 means "unset", in which case
 * we fall back to a neutral so the stripe never renders as transparent.
 */
private fun calendarColor(packed: Int): Color =
    if (packed == 0) Color(0xFF6B7A8F) else Color(packed).copy(alpha = 1f)

private fun formatEventTime(event: CalendarEventEntity): String {
    val start = Date(event.startTime)
    val dayLabel = when {
        isSameDay(event.startTime, System.currentTimeMillis()) -> "Today"
        isSameDay(event.startTime, System.currentTimeMillis() + 86_400_000L) -> "Tomorrow"
        else -> SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(start)
    }

    if (event.isAllDay) return "$dayLabel · All day"

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return "$dayLabel · ${timeFormat.format(start)}–${timeFormat.format(Date(event.endTime))}"
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val calA = Calendar.getInstance().apply { timeInMillis = a }
    val calB = Calendar.getInstance().apply { timeInMillis = b }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}
