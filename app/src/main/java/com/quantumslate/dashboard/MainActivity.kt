package com.quantumslate.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.ui.navigation.QuantumSlateNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Bible §5: the user picks which mode the app opens in.
        val startMode = when (preferencesManager.getDefaultUiMode()) {
            PreferencesManager.UiMode.MINIMALIST -> 0
            PreferencesManager.UiMode.DATA_DENSE -> 1
            PreferencesManager.UiMode.RETRO -> 2
            PreferencesManager.UiMode.QUANTUM_EFFECT -> 3
        }

        setContent {
            QuantumSlateApp(startMode = startMode)
        }
    }
}

@Composable
fun QuantumSlateApp(startMode: Int = 0) {
    QuantumSlateNavHost(startMode = startMode)
}
