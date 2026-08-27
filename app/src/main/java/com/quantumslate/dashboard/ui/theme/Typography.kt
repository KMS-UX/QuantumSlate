package com.quantumslate.dashboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.quantumslate.dashboard.R

// ==================== Font families ====================

/** Playfair Display — the newspaper masthead face. */
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_medium, FontWeight.Medium),
    Font(R.font.playfair_display_bold, FontWeight.SemiBold),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

/** Old Standard TT — headline/section face, closest to period newspaper type. */
val OldStandard = FontFamily(
    Font(R.font.old_standard_tt_regular, FontWeight.Normal),
    Font(R.font.old_standard_tt_bold, FontWeight.Bold)
)

/** Crimson Text — newspaper body copy. */
val CrimsonText = FontFamily(
    Font(R.font.crimson_text_regular, FontWeight.Normal),
    Font(R.font.crimson_text_semibold, FontWeight.SemiBold)
)

/**
 * QuantumEffect substitute face.
 *
 * The source design system specifies Orbitron (display) and Share Tech Mono (UI). Neither
 * is bundled, so the system monospace stands in, carrying the character through wide
 * letter-spacing and uppercase rather than through the typeface itself. Dropping the real
 * fonts into res/font and pointing these at them is a drop-in change.
 */
val QuantumMono = FontFamily.Monospace

// ==================== Mode typographies ====================

/**
 * Mode A — Ultra-Minimalist (Bible §3).
 * Large type, light weights, generous line height. Deliberately few sizes.
 */
val MinimalistTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Thin,
        fontSize = 96.sp,
        lineHeight = 100.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        lineHeight = 66.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        letterSpacing = 0.4.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )
)

/**
 * Mode C — Retro 1950s Newspaper (Bible §3).
 * The bundled vintage faces are actually used here; previously every style fell back to
 * the generic serif, which is why the mode read as unstyled.
 */
val RetroTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        lineHeight = 50.sp,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = OldStandard,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OldStandard,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = OldStandard,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 1.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CrimsonText,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CrimsonText,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CrimsonText,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = OldStandard,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)

/**
 * Mode B — Data-Dense (Bible §3).
 * Compact, tabular. Monospace for anything numeric so columns align.
 */
val DataDenseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.6.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp
    )
)

/**
 * Mode D — QuantumEffect.
 * Follows the source system's rule: display styles are uppercase with wide tracking
 * (`--tracking-caps: 0.14em`), body text is monospace.
 */
val QuantumEffectTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = 4.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 3.sp
    ),
    titleMedium = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 2.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = QuantumMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp
    )
)

/** Default, used before a mode is resolved. */
val Typography = MinimalistTypography
