package com.quantumslate.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            QuantumSlateApp()
        }
    }
}

@Composable
fun QuantumSlateApp() {
    var currentDashboard by remember { mutableStateOf(0) } // 0 = Minimalist, 1 = DataDense, 2 = Retro
    
    val onSwipeLeft = {
        if (currentDashboard < 2) currentDashboard++
    }
    
    val onSwipeRight = {
        if (currentDashboard > 0) currentDashboard--
    }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(currentDashboard) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Swipe handling will be implemented properly
                    }
                )
            },
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentDashboard) {
            0 -> MinimalistDashboard()
            1 -> DataDenseDashboard()
            2 -> RetroNewspaperDashboard()
        }
    }
}
