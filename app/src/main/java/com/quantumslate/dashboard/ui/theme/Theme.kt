package com.quantumslate.dashboard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.quantumslate.dashboard.data.local.PreferencesManager

private val MinimalistLightColorScheme = lightColorScheme(
    primary = MinimalistPrimary,
    onPrimary = MinimalistOnPrimary,
    background = MinimalistBackground,
    surface = MinimalistSurface,
    onBackground = MinimalistOnBackground,
    onSurface = MinimalistOnBackground
)

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = DarkMinimalistPrimary,
    onPrimary = DarkMinimalistOnPrimary,
    background = DarkMinimalistBackground,
    surface = DarkMinimalistSurface,
    onBackground = DarkMinimalistOnBackground,
    onSurface = DarkMinimalistOnBackground
)

private val RetroColorScheme = lightColorScheme(
    primary = RetroPrimary,
    onPrimary = Color.White,
    background = RetroBackground,
    surface = RetroSurface,
    onBackground = RetroOnBackground,
    onSurface = RetroOnBackground,
    secondary = RetroAccent
)

/**
 * QuantumEffect: the pixel-art sci-fi mode.
 *
 * Dark by design — the source design system defines no light variant, so this scheme is
 * used regardless of the app's light/dark setting.
 */
private val QuantumEffectColorScheme = darkColorScheme(
    primary = QeTeal,
    onPrimary = QeVoid0,
    secondary = QeQuantumPurple,
    onSecondary = QeFg1,
    tertiary = QeAtomGold,
    background = QeVoid0,
    onBackground = QeFg1,
    surface = QeVoid1,
    onSurface = QeFg1,
    surfaceVariant = QeVoid2,
    onSurfaceVariant = QeFg2,
    outline = QeBorderPanel,
    outlineVariant = QeBorderBright,
    error = QeAlertRed,
    onError = QeFg1
)

private val DataDenseColorScheme = lightColorScheme(
    primary = DataDensePrimary,
    onPrimary = Color.White,
    background = DataDenseBackground,
    surface = DataDenseSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    secondary = DataDenseSecondary,
    tertiary = DataDenseWarning,
    error = DataDenseError
)

@Composable
fun QuantumSlateTheme(
    uiMode: PreferencesManager.UiMode = PreferencesManager.UiMode.MINIMALIST,
    darkMode: PreferencesManager.DarkMode = PreferencesManager.DarkMode.AUTO,
    content: @Composable () -> Unit
) {
    val isSystemInDark = isSystemInDarkTheme()
    val useDarkTheme = when (darkMode) {
        PreferencesManager.DarkMode.LIGHT -> false
        PreferencesManager.DarkMode.DARK -> true
        PreferencesManager.DarkMode.AUTO -> isSystemInDark
    }

    val colorScheme = when (uiMode) {
        PreferencesManager.UiMode.MINIMALIST -> {
            if (useDarkTheme) MinimalistDarkColorScheme else MinimalistLightColorScheme
        }
        PreferencesManager.UiMode.RETRO -> RetroColorScheme
        PreferencesManager.UiMode.DATA_DENSE -> DataDenseColorScheme
        PreferencesManager.UiMode.QUANTUM_EFFECT -> QuantumEffectColorScheme
    }

    val typography = when (uiMode) {
        PreferencesManager.UiMode.MINIMALIST -> MinimalistTypography
        PreferencesManager.UiMode.RETRO -> RetroTypography
        PreferencesManager.UiMode.DATA_DENSE -> DataDenseTypography
        PreferencesManager.UiMode.QUANTUM_EFFECT -> QuantumEffectTypography
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            val lightStatusIcons = when (uiMode) {
                PreferencesManager.UiMode.QUANTUM_EFFECT -> false
                PreferencesManager.UiMode.RETRO -> true
                else -> !useDarkTheme
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightStatusIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
