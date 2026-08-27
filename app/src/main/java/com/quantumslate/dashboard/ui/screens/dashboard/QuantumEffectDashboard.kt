package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.data.local.DashboardWidget
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.permissions.rememberCalendarPermissionRequester
import com.quantumslate.dashboard.ui.theme.QeAlertRed
import com.quantumslate.dashboard.ui.theme.QeAtomGold
import com.quantumslate.dashboard.ui.theme.QeElectricBlue
import com.quantumslate.dashboard.ui.theme.QeFg2
import com.quantumslate.dashboard.ui.theme.QeQuantumPurple
import com.quantumslate.dashboard.ui.theme.QeSuccessGreen
import com.quantumslate.dashboard.ui.theme.QeTeal
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mode D — QuantumEffect.
 *
 * A HUD read of the same data the other modes show, built from the Quantum Effect Design
 * System's own primitives (QePanel, QeStatBar) and palette. Each widget becomes a
 * corner-ticked panel with an accent drawn from the source system's semantic roles:
 * gold for headings, teal for sections, purple for brand, red for alerts.
 */
@Composable
fun QuantumEffectDashboard(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {}
) {
    QuantumSlateTheme(uiMode = PreferencesManager.UiMode.QUANTUM_EFFECT) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val vm: DashboardViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsState()

            var showAddFlight by remember { mutableStateOf(false) }
            var showAddFeed by remember { mutableStateOf(false) }
            val requestCalendarPermission = rememberCalendarPermissionRequester { granted ->
                if (granted) vm.onCalendarPermissionGranted()
            }

            if (showAddFlight) {
                AddFlightDialog(
                    onDismiss = { showAddFlight = false },
                    onConfirm = { vm.addTrackedFlight(it); showAddFlight = false },
                    existingFlights = vm.trackedFlights
                )
            }
            if (showAddFeed) {
                AddFeedDialog(
                    onDismiss = { showAddFeed = false },
                    onConfirm = { vm.addRssFeed(it); showAddFeed = false },
                    existingFeeds = vm.rssFeeds
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- Header ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QUANTUMSLATE",
                            style = MaterialTheme.typography.headlineLarge,
                            color = QeAtomGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("EEEE dd MMM yyyy", Locale.getDefault())
                                .format(Date()).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = QeFg2
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.widgetLayout.isEnabled(DashboardWidget.MASCOT)) {
                            MascotWidget(
                                mascotState = uiState.mascotState,
                                size = 72,
                                animationsEnabled = uiState.mascotAnimationsEnabled
                            )
                        }
                        IconButton(onClick = { vm.refreshAll() }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = QeTeal)
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings", tint = QeTeal)
                        }
                    }
                }

                // ---- Time / Weather ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    QePanel(
                        title = "Chrono",
                        accent = QeElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        TimeDateWidget()
                    }

                    QePanel(
                        title = "Atmos",
                        accent = QeTeal,
                        modifier = Modifier.weight(1f)
                    ) {
                        val w = uiState.weather
                        if (w == null) {
                            Text(
                                text = uiState.weatherError ?: "NO SIGNAL",
                                style = MaterialTheme.typography.bodySmall,
                                color = QeFg2
                            )
                        } else {
                            Text(
                                text = "${w.temperature.toInt()}°",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = w.condition.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = QeFg2
                            )
                            QeStatBar(
                                label = "Humidity",
                                value = w.humidity.toFloat(),
                                max = 100f,
                                to = QeElectricBlue
                            )
                        }
                    }
                }

                // ---- Calendar ----
                if (uiState.widgetLayout.isEnabled(DashboardWidget.CALENDAR)) {
                    QePanel(title = "Mission Log", accent = QeQuantumPurple) {
                        when {
                            uiState.calendarPermissionMissing -> Text(
                                text = "CALENDAR ACCESS REQUIRED — TAP REFRESH AFTER GRANTING",
                                style = MaterialTheme.typography.bodySmall,
                                color = QeAtomGold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            uiState.calendarEvents.isEmpty() -> Text(
                                text = "NO OBJECTIVES IN RANGE",
                                style = MaterialTheme.typography.bodySmall,
                                color = QeFg2
                            )
                            else -> uiState.calendarEvents.take(3).forEach { e ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "▸ ${e.title}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                            .format(Date(e.startTime)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = QeTeal
                                    )
                                }
                            }
                        }
                        if (uiState.calendarPermissionMissing) {
                            IconButton(onClick = requestCalendarPermission) {
                                Icon(Icons.Default.Add, "Grant calendar access", tint = QeAtomGold)
                            }
                        }
                    }
                }

                // ---- Flights ----
                if (uiState.widgetLayout.isEnabled(DashboardWidget.FLIGHTS)) {
                    QePanel(title = "Transit", accent = QeAtomGold) {
                        if (uiState.flights.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NO VESSELS TRACKED",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = QeFg2
                                )
                                IconButton(onClick = { showAddFlight = true }) {
                                    Icon(Icons.Default.Add, "Track a flight", tint = QeAtomGold)
                                }
                            }
                        } else {
                            uiState.flights.forEach { f ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = f.flightNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${f.departureAirport}→${f.arrivalAirport}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QeFg2
                                    )
                                    Text(
                                        text = f.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor(f.status)
                                    )
                                }
                            }
                        }

                        // The free flight tier is small enough that the remaining allowance is
                        // genuinely operational information, so it gets a HUD meter.
                        uiState.flightRequestsRemaining?.let { left ->
                            Spacer(modifier = Modifier.height(4.dp))
                            QeStatBar(
                                label = "API Budget",
                                value = left.toFloat(),
                                max = 95f,
                                to = if (left < 15) QeAlertRed else QeSuccessGreen
                            )
                        }
                    }
                }

                // ---- News ----
                if (uiState.widgetLayout.isEnabled(DashboardWidget.NEWS)) {
                    QePanel(title = "Comms", accent = QeTeal) {
                        if (uiState.newsArticles.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NO INCOMING TRANSMISSIONS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = QeFg2
                                )
                                IconButton(onClick = { showAddFeed = true }) {
                                    Icon(Icons.Default.Add, "Add news feed", tint = QeTeal)
                                }
                            }
                        } else {
                            uiState.newsArticles.take(5).forEach { a ->
                                Text(
                                    text = "▸ ${a.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ---- Spotify ----
                uiState.spotifyTrack?.takeIf {
                    uiState.widgetLayout.isEnabled(DashboardWidget.SPOTIFY)
                }?.let { t ->
                    QePanel(title = "Audio", accent = QeSuccessGreen) {
                        Text(
                            text = t.trackName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = t.artistName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = QeFg2
                        )
                        if (t.durationMs > 0) {
                            QeStatBar(
                                label = "Progress",
                                value = t.progressMs.toFloat(),
                                max = t.durationMs.toFloat(),
                                to = QeSuccessGreen,
                                showValue = false
                            )
                        }
                    }
                }

                // Clearance for the mode indicator pinned at the bottom of the pager.
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}

/** Maps a flight status onto the source system's hazard colour scale. */
private fun statusColor(status: String) = when {
    status.contains("cancel", true) || status.contains("divert", true) -> QeAlertRed
    status.contains("delay", true) -> QeAtomGold
    status.contains("land", true) || status.contains("active", true) -> QeSuccessGreen
    else -> QeFg2
}
