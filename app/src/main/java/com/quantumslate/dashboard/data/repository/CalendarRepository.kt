package com.quantumslate.dashboard.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.quantumslate.dashboard.data.local.CalendarDao
import com.quantumslate.dashboard.data.local.CalendarEventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads calendar events from the device's own calendar provider.
 *
 * The Bible specifies the Google Calendar API, but every calendar the user has signed into —
 * Google included — is already synced into [CalendarContract]. Reading it locally gives the
 * same user-facing result with no OAuth, no Cloud Console project, and no network call, and
 * it keeps working offline. This divergence is recorded in progress.md.
 */
@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
    @ApplicationContext private val context: Context
) {

    companion object {
        /** Bible §8: fetch the next 7 days of events. */
        private val LOOKAHEAD_MS = TimeUnit.DAYS.toMillis(7)

        private val PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.CALENDAR_COLOR,
            CalendarContract.Instances.ALL_DAY
        )

        private const val IDX_ID = 0
        private const val IDX_TITLE = 1
        private const val IDX_BEGIN = 2
        private const val IDX_END = 3
        private const val IDX_CALENDAR_NAME = 4
        private const val IDX_COLOR = 5
        private const val IDX_ALL_DAY = 6
    }

    /** Upcoming events from cache, newest state first. Safe to collect without permission. */
    val upcomingEvents: Flow<List<CalendarEventEntity>> =
        calendarDao.getUpcomingEvents(System.currentTimeMillis())
            .flowOn(Dispatchers.IO)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Queries the calendar provider for the next 7 days and replaces the cache.
     *
     * Uses the Instances table rather than Events so that recurring events are expanded into
     * their individual occurrences — querying Events directly would return the recurrence
     * rule once and miss every repeat.
     */
    suspend fun fetchAndCacheEvents(): Result<List<CalendarEventEntity>> {
        return withContext(Dispatchers.IO) {
            if (!hasPermission()) {
                return@withContext Result.failure(
                    SecurityException("Calendar permission not granted")
                )
            }

            try {
                val now = System.currentTimeMillis()
                val events = queryInstances(now, now + LOOKAHEAD_MS)

                calendarDao.clearEvents()
                if (events.isNotEmpty()) {
                    calendarDao.insertEvents(events)
                }

                Result.success(events)
            } catch (e: SecurityException) {
                // Permission can be revoked between the check and the query.
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCachedEvents(): List<CalendarEventEntity> =
        withContext(Dispatchers.IO) {
            calendarDao.getUpcomingEventsOnce(System.currentTimeMillis())
        }

    private fun queryInstances(startMs: Long, endMs: Long): List<CalendarEventEntity> {
        // Instances requires the time window to be appended to the URI, not passed as args.
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startMs.toString())
            .appendPath(endMs.toString())
            .build()

        val cursor: Cursor = context.contentResolver.query(
            uri,
            PROJECTION,
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        ) ?: return emptyList()

        return cursor.use { c ->
            buildList {
                while (c.moveToNext()) {
                    val begin = c.getLong(IDX_BEGIN)
                    add(
                        CalendarEventEntity(
                            // Instance start is part of the key so recurring occurrences
                            // do not collapse onto a single row.
                            id = "${c.getLong(IDX_ID)}_$begin",
                            title = c.getString(IDX_TITLE) ?: "(No title)",
                            startTime = begin,
                            endTime = c.getLong(IDX_END),
                            calendarName = c.getString(IDX_CALENDAR_NAME).orEmpty(),
                            color = c.getInt(IDX_COLOR),
                            isAllDay = c.getInt(IDX_ALL_DAY) == 1
                        )
                    )
                }
            }
        }
    }
}
