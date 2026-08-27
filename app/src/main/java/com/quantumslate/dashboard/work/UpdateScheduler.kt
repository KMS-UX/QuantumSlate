package com.quantumslate.dashboard.work

import android.content.Context
import androidx.work.*
import com.quantumslate.dashboard.data.local.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WorkManager scheduling for dashboard updates.
 * Handles different update frequencies: Daily, Ambient, Real-time.
 */
@Singleton
class UpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val UPDATE_WORK_TAG = "dashboard_update"
        private const val UNIQUE_WORK_NAME = "quantumslate_dashboard_sync"
        
        // Update intervals
        private const val DAILY_INTERVAL_HOURS = 24L
        private const val AMBIENT_INTERVAL_MINUTES = 30L
        private const val REALTIME_INTERVAL_MINUTES = 5L
    }

    /**
     * Schedules or reschedules background updates based on current settings.
     * Call this when settings change or app starts.
     */
    fun scheduleUpdates() {
        scope.launch {
            try {
                val updateFrequency = preferencesManager.getUpdateFrequency() ?: "daily"
                enqueueFor(updateFrequency)
            } catch (e: Exception) {
                // Fall back to the safest, lowest-power schedule.
                enqueueFor("daily")
            }
        }
    }

    private fun enqueueFor(updateFrequency: String) {
        // Cancel whichever flavour of work is currently registered under this name.
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)

        // Real-time needs a foreground service (WorkManager's floor is 15 minutes); every
        // other mode must make sure that service is not left running.
        if (updateFrequency.lowercase() == "realtime") {
            RealtimeSyncService.start(context)
        } else {
            RealtimeSyncService.stop(context)
        }

        when (updateFrequency.lowercase()) {
            "ambient" -> workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                createAmbientWorkRequest()
            )
            "realtime" -> workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                createRealtimeWorkRequest()
            )
            else -> workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                createDailyWorkRequest()
            )
        }
    }

    private fun createDailyWorkRequest(): OneTimeWorkRequest {
        // Schedule for user-defined time, default to 6 AM
        val autoUpdateTime = preferencesManager.getAutoUpdateTime()
        val (hour, minute) = autoUpdateTime.split(":").map { it.toInt() }
        
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        
        var delay = calendar.timeInMillis - now
        if (delay < 0) {
            // Already passed today, schedule for tomorrow
            delay += TimeUnit.DAYS.toMillis(1)
        }
        
        return OneTimeWorkRequestBuilder<DashboardUpdateWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(UPDATE_WORK_TAG)
            .build()
    }

    private fun createAmbientWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DashboardUpdateWorker>(
            AMBIENT_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .addTag(UPDATE_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
    }

    private fun createRealtimeWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DashboardUpdateWorker>(
            REALTIME_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .addTag(UPDATE_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
    }

    /**
     * Triggers an immediate manual refresh.
     */
    fun triggerManualRefresh() {
        val manualWork = OneTimeWorkRequestBuilder<DashboardUpdateWorker>()
            .addTag("manual_refresh")
            .build()
        
        workManager.enqueue(manualWork)
    }

    /**
     * Cancels all scheduled updates.
     */
    fun cancelUpdates() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        workManager.cancelAllWorkByTag(UPDATE_WORK_TAG)
    }
}
