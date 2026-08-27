package com.quantumslate.dashboard.work

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.quantumslate.dashboard.MainActivity
import com.quantumslate.dashboard.R
import com.quantumslate.dashboard.data.repository.FlightRepository
import com.quantumslate.dashboard.data.repository.SpotifyRepository
import com.quantumslate.dashboard.data.repository.WeatherRepository
import com.quantumslate.dashboard.notification.NotificationManager as QsNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Foreground service backing Real-Time update mode (Bible §4).
 *
 * WorkManager cannot poll faster than every 15 minutes, so the Bible's 1–5 minute cadence
 * needs a foreground service. That is a deliberate battery trade, which is why the mode is
 * opt-in and why the ongoing notification states the cost plainly rather than hiding it.
 *
 * Flights are excluded from this loop on purpose: the flight provider's free tier allows
 * 100 requests a *month*, so minute-level polling would exhaust it in under two hours.
 * FlightPollingPolicy continues to govern flights regardless of update mode.
 */
@AndroidEntryPoint
class RealtimeSyncService : Service() {

    @Inject lateinit var weatherRepository: WeatherRepository
    @Inject lateinit var spotifyRepository: SpotifyRepository
    @Inject lateinit var flightRepository: FlightRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startPolling()
        // Restart if killed: the user explicitly asked for live data.
        return START_STICKY
    }

    private fun startPolling() {
        scope.launch {
            while (isActive) {
                // Spotify moves fastest and is cheapest to poll (Bible §4: 30s when playing).
                runCatching { spotifyRepository.fetchAndCachePlayback() }
                runCatching { weatherRepository.fetchAndCacheWeather(0.0, 0.0) }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, QsNotificationManager.CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle("Real-time updates active")
            // Bible §4 requires the high-battery warning to be visible, not buried.
            .setContentText("Updating every minute. This uses noticeably more battery.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(open)
            .build()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 9100
        private val POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1)

        fun start(context: Context) {
            val intent = Intent(context, RealtimeSyncService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RealtimeSyncService::class.java))
        }
    }
}
