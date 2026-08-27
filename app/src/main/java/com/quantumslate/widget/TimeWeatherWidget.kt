package com.quantumslate.dashboard.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.quantumslate.dashboard.MainActivity
import com.quantumslate.dashboard.R
import com.quantumslate.dashboard.data.local.WeatherDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home screen widget: Time & Weather.
 *
 * Reads the same Room cache the in-app weather widget uses, so the home screen can never
 * disagree with the dashboard, and it keeps working offline.
 */
class TimeWeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == REFRESH_ACTION) {
            // Redraw every placed instance from the current cache.
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, TimeWeatherWidget::class.java))
                .forEach { updateAppWidget(context, manager, it) }
        }
    }

    companion object {
        const val REFRESH_ACTION = "com.quantumslate.dashboard.REFRESH_WIDGET"
    }
}

/**
 * Hilt cannot inject into a BroadcastReceiver's static update path, so the DAO is pulled
 * from the singleton component explicitly.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun weatherDao(): WeatherDao
}

/** Widget updates must not block the broadcast thread. */
private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_time_weather)

    views.setTextViewText(
        R.id.widget_time_text,
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    )
    views.setTextViewText(
        R.id.widget_date_text,
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    )
    views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_placeholder)

    views.setOnClickPendingIntent(
        R.id.widget_container,
        PendingIntent.getActivity(
            context,
            appWidgetId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    )

    views.setOnClickPendingIntent(
        R.id.widget_refresh_button,
        PendingIntent.getBroadcast(
            context,
            appWidgetId,
            Intent(context, TimeWeatherWidget::class.java)
                .setAction(TimeWeatherWidget.REFRESH_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    )

    // Publish the clock immediately, then fill in the temperature when the cache read
    // returns, so time is never held up by disk I/O.
    views.setTextViewText(R.id.widget_weather_text, "--\u00B0")
    appWidgetManager.updateAppWidget(appWidgetId, views)

    widgetScope.launch {
        val temperature = runCatching {
            EntryPointAccessors
                .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                .weatherDao()
                .getWeatherOnce()
        }.getOrNull()?.let { "${it.temperature.toInt()}\u00B0" } ?: "--\u00B0"

        views.setTextViewText(R.id.widget_weather_text, temperature)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
