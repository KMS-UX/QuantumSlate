package com.quantumslate.dashboard.data.local

/**
 * Which dashboard widgets are shown, and in what order (Bible §5).
 *
 * Kept as one small model rather than scattered booleans so the long-press config sheet and
 * the Settings list read and write exactly the same state.
 */
enum class DashboardWidget(val key: String, val label: String) {
    TIME("time", "Time & Date"),
    WEATHER("weather", "Weather"),
    CALENDAR("calendar", "Calendar"),
    NEWS("news", "News"),
    FLIGHTS("flights", "Flights"),
    SPOTIFY("spotify", "Now Playing"),
    MASCOT("mascot", "Mascot");

    companion object {
        fun fromKey(key: String): DashboardWidget? = entries.firstOrNull { it.key == key }
    }
}

/**
 * Enabled state plus display order.
 *
 * [order] holds every widget exactly once; enabling/disabling does not reorder, so a widget
 * toggled off and on again returns to where the user put it.
 */
data class WidgetLayout(
    val enabled: Set<DashboardWidget> = DashboardWidget.entries.toSet(),
    val order: List<DashboardWidget> = DashboardWidget.entries.toList()
) {
    fun isEnabled(widget: DashboardWidget): Boolean = widget in enabled

    /** Widgets to render, in user order, skipping disabled ones. */
    fun visible(): List<DashboardWidget> = order.filter { it in enabled }

    fun toggled(widget: DashboardWidget): WidgetLayout =
        copy(enabled = if (widget in enabled) enabled - widget else enabled + widget)

    /** Moves [widget] one slot toward the start (or end) of the order. */
    fun moved(widget: DashboardWidget, up: Boolean): WidgetLayout {
        val idx = order.indexOf(widget)
        if (idx < 0) return this
        val target = if (up) idx - 1 else idx + 1
        if (target !in order.indices) return this
        val next = order.toMutableList()
        next[idx] = next[target].also { next[target] = next[idx] }
        return copy(order = next)
    }
}
