package com.quantumslate.dashboard.ui.navigation

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.components.DashboardViewModel
import com.quantumslate.dashboard.ui.components.ModeIndicator
import com.quantumslate.dashboard.ui.components.WidgetConfigSheet
import com.quantumslate.dashboard.ui.components.QuantumEffectDashboard
import com.quantumslate.dashboard.ui.components.RetroNewspaperDashboard

/**
 * The modes the app ships.
 *
 * Narrowed from four to two on 2026-08-27: Minimalist and Data-Dense differed from these
 * only in styling, and every extra mode multiplied the places each widget and each fix had
 * to be wired. Their dashboards remain in the repo, unreferenced, for a possible spin-off.
 */
val DASHBOARD_MODES: List<PreferencesManager.UiMode> = PreferencesManager.UiMode.shipping

/** Horizontal distance a drag must cover before it counts as a mode switch. */
private const val SWIPE_THRESHOLD_PX = 120f

/**
 * Hosts the shipping dashboard modes and the horizontal swipe gesture between them.
 *
 * Each mode applies its own theme internally, so this composable stays theme-neutral and
 * only owns the gesture, the mode indicator, and dispatch to the selected dashboard.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardPager(
    currentMode: Int,
    onModeChange: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The gesture lambda is created once (pointerInput keyed on Unit) but must always see
    // the latest mode and callback, hence rememberUpdatedState.
    val mode by rememberUpdatedState(currentMode)
    val onChange by rememberUpdatedState(onModeChange)

    // Pull-to-refresh lives here rather than in each dashboard (Bible §3 requires it on all
    // modes, and one host means one consistent gesture).
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val uiState by dashboardViewModel.uiState.collectAsState()
    var showWidgetConfig by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { dashboardViewModel.refreshAll() }
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Accumulate the drag and commit on release, so one flick moves exactly one mode.
                var dragTotal = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onDragEnd = {
                        when {
                            dragTotal <= -SWIPE_THRESHOLD_PX && mode < DASHBOARD_MODES.lastIndex ->
                                onChange(mode + 1)
                            dragTotal >= SWIPE_THRESHOLD_PX && mode > 0 ->
                                onChange(mode - 1)
                        }
                    },
                    onDragCancel = { dragTotal = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        dragTotal += dragAmount
                        change.consume()
                    }
                )
            }
            .pointerInput(Unit) {
                // requireUnconsumed = false: let the scrollable content receive the gesture
                // too, so a long-press never blocks a vertical scroll.
                detectTapGestures(
                    onLongPress = { showWidgetConfig = true }
                )
            }
            .pullRefresh(pullRefreshState),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (DASHBOARD_MODES.getOrNull(currentMode)) {
                PreferencesManager.UiMode.QUANTUM_EFFECT ->
                    // QuantumEffect is dark by design and has no light variant.
                    QuantumEffectDashboard(onNavigateToSettings = onNavigateToSettings)

                else -> RetroNewspaperDashboard(
                    onNavigateToSettings = onNavigateToSettings,
                    darkMode = uiState.darkMode
                )
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (showWidgetConfig) {
                WidgetConfigSheet(
                    layout = uiState.widgetLayout,
                    onToggle = dashboardViewModel::toggleWidget,
                    onMove = dashboardViewModel::moveWidget,
                    onRefreshAll = {
                        dashboardViewModel.refreshAll()
                        showWidgetConfig = false
                    },
                    onDismiss = { showWidgetConfig = false }
                )
            }

            ModeIndicator(
                modeCount = DASHBOARD_MODES.size,
                currentMode = currentMode,
                onModeSelected = onModeChange,
                modeNames = DASHBOARD_MODES.map { it.displayName },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
