package com.quantumslate.dashboard.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quantumslate.dashboard.MainActivity
import com.quantumslate.dashboard.R

/**
 * Phase 4: Notification Manager for QuantumSlate Dashboard
 * 
 * Handles push notifications for:
 * - Flight delays and status changes
 * - Severe weather warnings
 * - Breaking news alerts
 * - Sync errors and offline mode
 */
class NotificationManager(private val context: Context) {

    companion object {
        // Channel IDs
        const val CHANNEL_FLIGHTS = "flight_alerts"
        const val CHANNEL_WEATHER = "weather_warnings"
        const val CHANNEL_NEWS = "breaking_news"
        const val CHANNEL_SYSTEM = "system_notifications"
        
        // Notification IDs
        const val NOTIFICATION_FLIGHT_DELAY = 1001
        const val NOTIFICATION_FLIGHT_STATUS = 1002
        const val NOTIFICATION_WEATHER_WARNING = 2001
        const val NOTIFICATION_BREAKING_NEWS = 3001
        const val NOTIFICATION_SYNC_ERROR = 9001
        const val NOTIFICATION_OFFLINE_MODE = 9002
        
        // Channel names and descriptions
        private const val FLIGHT_CHANNEL_NAME = "Flight Alerts"
        private const val WEATHER_CHANNEL_NAME = "Weather Warnings"
        private const val NEWS_CHANNEL_NAME = "Breaking News"
        private const val SYSTEM_CHANNEL_NAME = "System Notifications"
    }

    /**
     * Create all notification channels (call on app startup)
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) ?: return

            // Flight Alerts Channel (High importance)
            val flightChannel = NotificationChannel(
                CHANNEL_FLIGHTS,
                FLIGHT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Flight delay and status change notifications"
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
            }
            notificationManager.createNotificationChannel(flightChannel)

            // Weather Warnings Channel (High importance)
            val weatherChannel = NotificationChannel(
                CHANNEL_WEATHER,
                WEATHER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Severe weather warnings and alerts"
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.YELLOW
            }
            notificationManager.createNotificationChannel(weatherChannel)

            // Breaking News Channel (Default importance)
            val newsChannel = NotificationChannel(
                CHANNEL_NEWS,
                NEWS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Breaking news headlines"
                enableVibration(false)
                enableLights(false)
            }
            notificationManager.createNotificationChannel(newsChannel)

            // System Notifications Channel (Low importance)
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                SYSTEM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sync errors, offline mode, and other system notifications"
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(systemChannel)
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Posts a notification, honouring the Android 13+ runtime permission.
     *
     * Silently drops the notification when the user has not granted POST_NOTIFICATIONS;
     * a missing notification is never worth crashing the background sync over.
     */
    private fun postNotification(notificationId: Int, notification: Notification) {
        if (!hasNotificationPermission()) return
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check and the post.
        }
    }

    /**
     * Show flight delay notification
     */
    fun showFlightDelayNotification(
        flightNumber: String,
        airline: String,
        delayMinutes: Int,
        newDepartureTime: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "flights")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FLIGHTS)
            .setSmallIcon(R.drawable.ic_flight_placeholder)
            .setContentTitle("Flight $flightNumber Delayed")
            .setContentText("$airline delayed by $delayMinutes minutes. New departure: $newDepartureTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        postNotification(NOTIFICATION_FLIGHT_DELAY, notification)
    }

    /**
     * Show flight status update notification
     */
    fun showFlightStatusNotification(
        flightNumber: String,
        status: String,
        gate: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "flights")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FLIGHTS)
            .setSmallIcon(R.drawable.ic_flight_placeholder)
            .setContentTitle("Flight $flightNumber: $status")
            .setContentText(gate?.let { "Gate: $it" } ?: "Tap to view details")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        postNotification(NOTIFICATION_FLIGHT_STATUS, notification)
    }

    /**
     * Show severe weather warning notification
     */
    fun showWeatherWarningNotification(
        alertType: String,
        severity: String,
        description: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "weather")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WEATHER)
            .setSmallIcon(R.drawable.ic_weather_placeholder)
            .setContentTitle("Weather Alert: $alertType")
            .setContentText("$severity - $description")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        postNotification(NOTIFICATION_WEATHER_WARNING, notification)
    }

    /**
     * Show breaking news notification
     */
    fun showBreakingNewsNotification(headline: String, source: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "news")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEWS)
            .setSmallIcon(R.drawable.ic_news_placeholder)
            .setContentTitle("Breaking News")
            .setContentText(headline)
            .setSubText(source)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        postNotification(NOTIFICATION_BREAKING_NEWS, notification)
    }

    /**
     * Show sync error notification
     */
    fun showSyncErrorNotification(errorMessage: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle("Sync Failed")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SYSTEM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        postNotification(NOTIFICATION_SYNC_ERROR, notification)
    }

    /**
     * Show offline mode notification
     */
    fun showOfflineModeNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_weather_placeholder)
            .setContentTitle("Offline Mode")
            .setContentText("Using cached data. Connect to internet for updates.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SYSTEM)
            .setOngoing(true)
            .build()

        postNotification(NOTIFICATION_OFFLINE_MODE, notification)
    }

    /**
     * Cancel a specific notification
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancel all notifications from this app
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
