package com.quantumslate.dashboard.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deletes cached rows the app will never show again (Bible §6: "Auto-expire cache after
 * 24 hours").
 *
 * `CacheManager` only ever classified data as stale for *display* purposes — nothing
 * removed it, so the database grew without bound and old rows could resurface. This is the
 * missing half.
 *
 * Weather and Spotify are single-row caches keyed by a fixed id, so they are expired by
 * age. Calendar events and flights expire by their own end/arrival time instead: an event
 * fetched an hour ago but finished yesterday is useless regardless of how fresh the fetch
 * was.
 */
@Singleton
class CacheExpiry @Inject constructor(
    private val weatherDao: WeatherDao,
    private val calendarDao: CalendarDao,
    private val newsDao: NewsDao,
    private val flightDao: FlightDao,
    private val spotifyDao: SpotifyDao
) {
    companion object {
        val MAX_AGE_MS: Long = TimeUnit.HOURS.toMillis(24)

        /** News is kept a little longer — a day-old headline is still readable content. */
        val NEWS_MAX_AGE_MS: Long = TimeUnit.DAYS.toMillis(3)

        /** Grace after an event ends / a flight lands before the row is dropped. */
        private val COMPLETED_GRACE_MS: Long = TimeUnit.HOURS.toMillis(6)
    }

    suspend fun purgeExpired(now: Long = System.currentTimeMillis()) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteWeatherOlderThan(now - MAX_AGE_MS)
            spotifyDao.deleteTracksOlderThan(now - MAX_AGE_MS)
            newsDao.deleteArticlesOlderThan(now - NEWS_MAX_AGE_MS)
            calendarDao.deleteEventsEndedBefore(now - COMPLETED_GRACE_MS)
            flightDao.deleteFlightsArrivedBefore(now - COMPLETED_GRACE_MS)
        }
    }
}
