package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quantumslate.dashboard.data.local.DashboardWidget
import com.quantumslate.dashboard.data.local.WidgetLayout

/**
 * Long-press configuration sheet (Bible §3).
 *
 * Shows every widget with its enabled state and position. Reordering is exposed as explicit
 * up/down controls rather than drag-and-drop: this sheet is reachable by long-press on a
 * dashboard that is itself listening for horizontal swipes, and a drag handle inside it
 * would compete with that gesture. Buttons are also reachable with TalkBack, which a custom
 * drag reorder is not without extra work.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigSheet(
    layout: WidgetLayout,
    onToggle: (DashboardWidget) -> Unit,
    onMove: (DashboardWidget, Boolean) -> Unit,
    onRefreshAll: () -> Unit,
    onDismiss: () -> Unit,
    highlight: DashboardWidget? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure widgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefreshAll) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh all widgets")
                }
            }

            Text(
                text = "Turn widgets on or off, and set the order they appear in.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            layout.order.forEachIndexed { index, widget ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = widget.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (widget == highlight) FontWeight.Bold else FontWeight.Normal,
                        color = if (layout.isEnabled(widget)) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onMove(widget, true) },
                        enabled = index > 0
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move ${widget.label} up",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onMove(widget, false) },
                        enabled = index < layout.order.lastIndex
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move ${widget.label} down",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Switch(
                        checked = layout.isEnabled(widget),
                        onCheckedChange = { onToggle(widget) }
                    )
                }
            }
        }
    }
}
