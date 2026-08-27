package com.quantumslate.dashboard

import com.google.common.truth.Truth.assertThat
import com.quantumslate.dashboard.data.local.DashboardWidget
import com.quantumslate.dashboard.data.local.WidgetLayout
import org.junit.Test

class WidgetLayoutTest {

    @Test
    fun `every widget is enabled and ordered by default`() {
        val layout = WidgetLayout()
        assertThat(layout.order).containsExactlyElementsIn(DashboardWidget.entries).inOrder()
        assertThat(layout.visible()).hasSize(DashboardWidget.entries.size)
    }

    @Test
    fun `toggling removes then restores a widget`() {
        val layout = WidgetLayout()
        val off = layout.toggled(DashboardWidget.NEWS)
        assertThat(off.isEnabled(DashboardWidget.NEWS)).isFalse()
        assertThat(off.visible()).doesNotContain(DashboardWidget.NEWS)

        val on = off.toggled(DashboardWidget.NEWS)
        assertThat(on.isEnabled(DashboardWidget.NEWS)).isTrue()
    }

    @Test
    fun `disabling a widget does not change the order`() {
        // So a widget switched off and on again returns to where the user put it.
        val layout = WidgetLayout()
        val off = layout.toggled(DashboardWidget.NEWS)
        assertThat(off.order).isEqualTo(layout.order)
    }

    @Test
    fun `moving up swaps with the previous widget`() {
        val layout = WidgetLayout()
        val second = layout.order[1]
        val first = layout.order[0]
        val moved = layout.moved(second, up = true)
        assertThat(moved.order[0]).isEqualTo(second)
        assertThat(moved.order[1]).isEqualTo(first)
    }

    @Test
    fun `moving the first widget up is a no-op`() {
        val layout = WidgetLayout()
        assertThat(layout.moved(layout.order.first(), up = true).order).isEqualTo(layout.order)
    }

    @Test
    fun `moving the last widget down is a no-op`() {
        val layout = WidgetLayout()
        assertThat(layout.moved(layout.order.last(), up = false).order).isEqualTo(layout.order)
    }

    @Test
    fun `visible respects both order and enabled state`() {
        val layout = WidgetLayout()
            .toggled(DashboardWidget.SPOTIFY)
            .moved(DashboardWidget.MASCOT, up = true)
        assertThat(layout.visible()).doesNotContain(DashboardWidget.SPOTIFY)
        assertThat(layout.visible()).containsNoDuplicates()
    }

    @Test
    fun `keys round-trip so persisted layouts survive a restart`() {
        DashboardWidget.entries.forEach { widget ->
            assertThat(DashboardWidget.fromKey(widget.key)).isEqualTo(widget)
        }
        assertThat(DashboardWidget.fromKey("not_a_widget")).isNull()
    }
}
