package com.quantumslate.dashboard.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.quantumslate.dashboard.R
import com.quantumslate.dashboard.ui.theme.QuantumSlateTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Phase 4: Android Home Screen Widget - Time & Weather Compact Widget
 * 
 * Displays current time and weather condition in a compact 2x1 widget format.
 * Users can tap to open the full dashboard app.
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

    override fun onEnabled(context: Context) {
        // Enter relevant mode for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant mode for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == REFRESH_ACTION) {
            // Handle manual refresh
        }
    }

    companion object {
        const val REFRESH_ACTION = "com.quantumslate.dashboard.REFRESH_WIDGET"
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_time_weather)
    
    // Set current time
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    views.setTextViewText(R.id.widget_time_text, currentTime)
    
    // Set date (short format)
    val currentDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    views.setTextViewText(R.id.widget_date_text, currentDate)
    
    // Placeholder weather - will be populated from repository in production
    views.setTextViewText(R.id.widget_weather_text, "--°")
    views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_placeholder)
    
    // Set up click intent to open main app
    val intent = Intent(context, Class.forName("com.quantumslate.dashboard.MainActivity"))
    val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    
    // Set up refresh button
    val refreshIntent = Intent(REFRESH_ACTION).setPackage(context.packageName)
    val refreshPendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        refreshIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)
    
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
