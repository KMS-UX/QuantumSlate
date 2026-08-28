package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Row of dots showing which dashboard mode is active.
 *
 * Without this the three modes are indistinguishable from a stuck screen — the user has no
 * cue that swiping does anything. Dots are also tappable as a non-gesture way to switch
 * modes, which keeps mode switching reachable for accessibility services.
 */
@Composable
fun ModeIndicator(
    modeCount: Int,
    currentMode: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    /** Names announced to screen readers; falls back to positional wording if absent. */
    modeNames: List<String> = emptyList()
) {
    Row(
        modifier = modifier.padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(modeCount) { index ->
            val selected = index == currentMode
            Row(
                // 48dp touch target per Bible §13, with a smaller visible dot inside.
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onModeSelected(index) }
                    .semantics {
                        val name = modeNames.getOrNull(index)
                        contentDescription = (
                            name?.let { "$it dashboard" }
                                ?: "Dashboard mode ${index + 1} of $modeCount"
                            ) + if (selected) ", selected" else ""
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .size(if (selected) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                            }
                        )
                ) {}
            }
        }
    }
}
