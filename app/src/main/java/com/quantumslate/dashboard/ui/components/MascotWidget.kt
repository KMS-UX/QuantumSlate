package com.quantumslate.dashboard.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quantumslate.dashboard.R
import com.quantumslate.dashboard.data.local.MascotStateEntity
import com.quantumslate.dashboard.domain.model.MascotMood
import kotlinx.coroutines.delay

/**
 * Quantum Boy — the dashboard mascot.
 *
 * Deliberately **static at rest**: a dashboard is a screen that sits on display for hours,
 * so a continuously looping mascot would redraw forever for no informational gain. The
 * Bible's own budget (§6: under 5% battery per day) rules that out. Motion here is
 * *functional* — it happens when the mascot has something to report:
 *
 *  - a mood change swaps the pose with a short crossfade
 *  - the SLEEPY mood advances through its three drawn stages, so "just getting tired" and
 *    "fast asleep" read differently
 *
 * Everything else holds a single frame and costs nothing.
 */
@Composable
fun MascotWidget(
    mascotState: MascotStateEntity?,
    modifier: Modifier = Modifier,
    size: Int = 120,
    animationsEnabled: Boolean = true
) {
    val mood = mascotState?.mood?.toMascotMood() ?: MascotMood.NEUTRAL
    val stages = mood.stages()

    // Only a multi-stage mood animates, and only when animations are on.
    var stage by remember(mood) { mutableIntStateOf(0) }
    if (animationsEnabled && stages.size > 1) {
        LaunchedEffect(mood) {
            // Stepped, not interpolated: hold each drawn stage, then advance.
            while (true) {
                delay(STAGE_DURATION_MS)
                stage = (stage + 1) % stages.size
            }
        }
    }

    val frame = stages[stage.coerceIn(stages.indices)]

    Box(
        modifier = modifier
            .size(size.dp)
            .semantics { contentDescription = mood.describe() },
        contentAlignment = Alignment.Center
    ) {
        if (animationsEnabled) {
            Crossfade(
                targetState = frame,
                animationSpec = tween(durationMillis = 220, easing = LinearEasing),
                label = "mascot-pose"
            ) { res ->
                MascotImage(res, size)
            }
        } else {
            MascotImage(frame, size)
        }
    }
}

@Composable
private fun MascotImage(@DrawableRes res: Int, size: Int) {
    Image(
        painter = painterResource(id = res),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(size.dp)
    )
}

/** How long each stage of a multi-stage mood is held before advancing. */
private const val STAGE_DURATION_MS = 2_600L

/**
 * Poses drawn for each mood.
 *
 * SLEEPY is the only multi-stage mood: the source art is a labelled progression
 * (rub eye → yawn → curled), so it can express *how* sleepy rather than just "asleep".
 *
 * CONCERNED currently reuses the shoulders-drop pose, which the source sheet drew as
 * "fatigue onset". It reads as unhappy rather than specifically worried — a known
 * divergence recorded in progress.md, pending dedicated art.
 */
private fun MascotMood.stages(): List<Int> = when (this) {
    MascotMood.HAPPY -> listOf(R.drawable.quantum_boy_happy)
    MascotMood.EXCITED -> listOf(R.drawable.quantum_boy_excited)
    MascotMood.CONCERNED -> listOf(R.drawable.quantum_boy_concerned)
    MascotMood.NEUTRAL -> listOf(R.drawable.quantum_boy_neutral)
    MascotMood.SLEEPY -> listOf(
        R.drawable.quantum_boy_sleepy_1,
        R.drawable.quantum_boy_sleepy_2,
        R.drawable.quantum_boy_sleepy_3
    )
}

/** Bible §13: the mascot conveys state, so screen readers must get that state too. */
private fun MascotMood.describe(): String = when (this) {
    MascotMood.HAPPY -> "Mascot is waving, everything looks good"
    MascotMood.EXCITED -> "Mascot is excited, something is coming up soon"
    MascotMood.CONCERNED -> "Mascot looks concerned, something needs attention"
    MascotMood.SLEEPY -> "Mascot is sleepy, it is quiet right now"
    MascotMood.NEUTRAL -> "Mascot is standing by"
}

/** Tolerates legacy stored values ("happy_wave", "worried_look") from earlier builds. */
private fun String.toMascotMood(): MascotMood {
    val v = lowercase()
    return when {
        v.contains("happy") -> MascotMood.HAPPY
        v.contains("excite") || v.contains("danc") -> MascotMood.EXCITED
        v.contains("concern") || v.contains("worri") -> MascotMood.CONCERNED
        v.contains("sleep") -> MascotMood.SLEEPY
        else -> MascotMood.NEUTRAL
    }
}
