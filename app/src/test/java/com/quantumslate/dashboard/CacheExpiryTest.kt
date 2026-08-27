package com.quantumslate.dashboard

import com.google.common.truth.Truth.assertThat
import com.quantumslate.dashboard.data.local.CacheExpiry
import com.quantumslate.dashboard.data.local.CalendarDao
import com.quantumslate.dashboard.data.local.CalendarEventEntity
import com.quantumslate.dashboard.data.local.FlightDao
import com.quantumslate.dashboard.data.local.FlightEntity
import com.quantumslate.dashboard.data.local.NewsArticleEntity
import com.quantumslate.dashboard.data.local.NewsDao
import com.quantumslate.dashboard.data.local.SpotifyDao
import com.quantumslate.dashboard.data.local.SpotifyTrackEntity
import com.quantumslate.dashboard.data.local.WeatherDao
import com.quantumslate.dashboard.data.local.WeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Cache expiry did not exist before this repair: `CacheManager` labelled data stale for
 * display but nothing ever deleted a row, so the database grew without bound and expired
 * rows could resurface. These fakes record the cutoffs actually passed to the DAOs.
 */
class CacheExpiryTest {

    private val now = 1_800_000_000_000L

    private class FakeWeatherDao : WeatherDao {
        var cutoff: Long? = null
        override fun getWeather(): Flow<WeatherEntity?> = flowOf(null)
        override suspend fun getWeatherOnce(): WeatherEntity? = null
        override suspend fun insertWeather(weather: WeatherEntity) {}
        override suspend fun clearWeather() {}
        override suspend fun deleteWeatherOlderThan(cutoff: Long) { this.cutoff = cutoff }
    }

    private class FakeCalendarDao : CalendarDao {
        var cutoff: Long? = null
        override fun getUpcomingEvents(now: Long, limit: Int): Flow<List<CalendarEventEntity>> = flowOf(emptyList())
        override suspend fun getUpcomingEventsOnce(now: Long, limit: Int): List<CalendarEventEntity> = emptyList()
        override suspend fun insertEvents(events: List<CalendarEventEntity>) {}
        override suspend fun clearEvents() {}
        override suspend fun deleteEventsEndedBefore(cutoff: Long) { this.cutoff = cutoff }
    }

    private class FakeNewsDao : NewsDao {
        var cutoff: Long? = null
        override fun getNewsArticles(): Flow<List<NewsArticleEntity>> = flowOf(emptyList())
        override suspend fun getNewsArticlesOnce(): List<NewsArticleEntity> = emptyList()
        override suspend fun insertArticles(articles: List<NewsArticleEntity>) {}
        override suspend fun clearArticles() {}
        override suspend fun deleteArticlesOlderThan(cutoff: Long) { this.cutoff = cutoff }
    }

    private class FakeFlightDao : FlightDao {
        var cutoff: Long? = null
        override fun getTrackedFlights(): Flow<List<FlightEntity>> = flowOf(emptyList())
        override suspend fun getFlightOnce(flightNumber: String): FlightEntity? = null
        override suspend fun insertFlight(flight: FlightEntity) {}
        override suspend fun insertFlights(flights: List<FlightEntity>) {}
        override suspend fun deleteFlight(flightNumber: String) {}
        override suspend fun clearAllFlights() {}
        override suspend fun deleteFlightsArrivedBefore(cutoff: Long) { this.cutoff = cutoff }
    }

    private class FakeSpotifyDao : SpotifyDao {
        var cutoff: Long? = null
        override fun getCurrentTrack(): Flow<SpotifyTrackEntity?> = flowOf(null)
        override suspend fun getCurrentTrackOnce(): SpotifyTrackEntity? = null
        override suspend fun insertTrack(track: SpotifyTrackEntity) {}
        override suspend fun clearTracks() {}
        override suspend fun deleteTracksOlderThan(cutoff: Long) { this.cutoff = cutoff }
    }

    private val weather = FakeWeatherDao()
    private val calendar = FakeCalendarDao()
    private val news = FakeNewsDao()
    private val flights = FakeFlightDao()
    private val spotify = FakeSpotifyDao()

    private val expiry = CacheExpiry(weather, calendar, news, flights, spotify)

    @Test
    fun `weather expires at the 24 hour mark required by Bible section 6`() = runTest {
        expiry.purgeExpired(now)
        assertThat(weather.cutoff).isEqualTo(now - TimeUnit.HOURS.toMillis(24))
    }

    @Test
    fun `spotify uses the same 24 hour age cutoff`() = runTest {
        expiry.purgeExpired(now)
        assertThat(spotify.cutoff).isEqualTo(now - CacheExpiry.MAX_AGE_MS)
    }

    @Test
    fun `news is kept longer because a day-old headline is still readable`() = runTest {
        expiry.purgeExpired(now)
        assertThat(news.cutoff).isEqualTo(now - CacheExpiry.NEWS_MAX_AGE_MS)
        assertThat(CacheExpiry.NEWS_MAX_AGE_MS).isGreaterThan(CacheExpiry.MAX_AGE_MS)
    }

    @Test
    fun `calendar expires by event end time, not by fetch age`() = runTest {
        // An event fetched an hour ago but finished yesterday is useless however fresh the
        // fetch was, so the cutoff must be in the past relative to now, not 24h of age.
        expiry.purgeExpired(now)
        assertThat(calendar.cutoff).isLessThan(now)
        assertThat(calendar.cutoff).isGreaterThan(now - CacheExpiry.MAX_AGE_MS)
    }

    @Test
    fun `flights expire by arrival time with a grace period`() = runTest {
        expiry.purgeExpired(now)
        assertThat(flights.cutoff).isLessThan(now)
        assertThat(flights.cutoff).isEqualTo(calendar.cutoff)
    }

    @Test
    fun `every cache is purged in a single pass`() = runTest {
        expiry.purgeExpired(now)
        listOf(weather.cutoff, calendar.cutoff, news.cutoff, flights.cutoff, spotify.cutoff)
            .forEach { assertThat(it).isNotNull() }
    }
}
