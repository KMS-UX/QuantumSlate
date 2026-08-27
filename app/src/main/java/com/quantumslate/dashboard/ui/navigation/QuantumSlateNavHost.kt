package com.quantumslate.dashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quantumslate.dashboard.ui.components.DashboardViewModel
import com.quantumslate.dashboard.ui.screens.settings.SettingsScreen

/**
 * Route names for the app's two top-level destinations.
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
}

/**
 * Navigation host for QuantumSlate.
 *
 * The dashboard is a single destination that swipes between UI modes; only Settings is a
 * separate route. The selected mode is hoisted to this level with [rememberSaveable] so
 * that navigating to Settings and back returns the user to the mode they left, and so it
 * survives configuration changes.
 */
@Composable
fun QuantumSlateNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startMode: Int = 0
) {
    var currentMode by rememberSaveable { mutableIntStateOf(startMode) }

    // DashboardViewModel loads its data once, in init. Without this flag, entering an API
    // key in Settings and navigating back would leave every widget still showing the
    // failure it cached before the key existed.
    var settingsChanged by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier
    ) {
        composable(Routes.DASHBOARD) {
            // Scoped to this back stack entry, so it is the same instance the dashboards use.
            val dashboardViewModel: DashboardViewModel = hiltViewModel()

            LaunchedEffect(settingsChanged) {
                if (settingsChanged) {
                    dashboardViewModel.refreshAll()
                    settingsChanged = false
                }
            }

            DashboardPager(
                currentMode = currentMode,
                onModeChange = { currentMode = it },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSettingsChanged = { settingsChanged = true }
            )
        }
    }
}
