package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quantumslate.dashboard.ui.theme.QeBarTrack
import com.quantumslate.dashboard.ui.theme.QeBorderPanel
import com.quantumslate.dashboard.ui.theme.QeFg2
import com.quantumslate.dashboard.ui.theme.QeTeal
import com.quantumslate.dashboard.ui.theme.QeVoid1

/**
 * QuantumEffect HUD primitives, ported from the Quantum Effect Design System's
 * `components/hud/` (Panel, StatBar).
 *
 * Panel is described there as "dark, scanlined, corner-ticked", with `accent` recolouring
 * the title and corner ticks — that contract is preserved here.
 */

/** Scanline overlay: 2px dark rules every 4px, the mode's signature texture. */
private fun Modifier.scanlines(alpha: Float = 0.16f): Modifier = drawBehind {
    val step = 4f
    var y = 0f
    while (y < size.height) {
        drawRect(
            color = Color.Black.copy(alpha = alpha),
            topLeft = Offset(0f, y),
            size = Size(size.width, 1.5f)
        )
        y += step
    }
}

/**
 * Base container for every QuantumEffect surface.
 *
 * @param accent recolours the title and corner ticks
 * @param corners set false for a plain box
 */
@Composable
fun QePanel(
    title: String? = null,
    modifier: Modifier = Modifier,
    accent: Color = QeTeal,
    corners: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(QeVoid1)
            .scanlines()
            .drawBehind {
                // 1px panel border
                drawRect(
                    color = QeBorderPanel,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
                if (corners) {
                    // Corner ticks: short accent rules at each corner, not a full frame.
                    val len = 14f
                    val w = 2.5f
                    val c = accent
                    // top-left
                    drawRect(c, Offset(0f, 0f), Size(len, w))
                    drawRect(c, Offset(0f, 0f), Size(w, len))
                    // top-right
                    drawRect(c, Offset(size.width - len, 0f), Size(len, w))
                    drawRect(c, Offset(size.width - w, 0f), Size(w, len))
                    // bottom-left
                    drawRect(c, Offset(0f, size.height - w), Size(len, w))
                    drawRect(c, Offset(0f, size.height - len), Size(w, len))
                    // bottom-right
                    drawRect(c, Offset(size.width - len, size.height - w), Size(len, w))
                    drawRect(c, Offset(size.width - w, size.height - len), Size(w, len))
                }
            }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (title != null) {
                Text(
                    // The source system sets display styles in caps with wide tracking.
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

/**
 * Resource bar with a colour-coded gradient fill, per the source system's StatBar.
 *
 * Repurposed here as a generic ratio meter — used for things like how much of the flight
 * request allowance remains, or track progress.
 */
@Composable
fun QeStatBar(
    label: String,
    value: Float,
    max: Float,
    modifier: Modifier = Modifier,
    from: Color = QeBarTrack,
    to: Color = QeTeal,
    showValue: Boolean = true,
    barHeight: Int = 10
) {
    val ratio = if (max <= 0f) 0f else (value / max).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = QeFg2
            )
            if (showValue) {
                Text(
                    text = "${value.toInt()}/${max.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = QeFg2
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight.dp)
                .background(QeBarTrack)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (ratio > 0f) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(from, to)),
                        size = Size(size.width * ratio, size.height)
                    )
                }
                drawRect(
                    color = QeBorderPanel,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }
        }
    }
}
