package com.quantumslate.dashboard

import com.google.common.truth.Truth.assertThat
import com.quantumslate.dashboard.data.local.FlightEntity
import com.quantumslate.dashboard.data.remote.flight.FlightPollingPolicy
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * The polling policy is what makes a 100-request/month free tier usable, so its edges
 * matter more than most: poll too eagerly and the month's quota is gone, too lazily and the
 * user sees a stale departure time for a flight that has already moved.
 */
class FlightPollingPolicyTest {

    private val now = 1_800_000_000_000L // fixed clock; the policy takes `now` as a parameter

    private fun flight(
        status: String = "scheduled",
        departureOffsetMs: Long = TimeUnit.DAYS.toMillis(2),
        arrivalOffsetMs: Long = TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(3),
        fetchedAgoMs: Long = 0L
    ) = FlightEntity(
        flightNumber = "BA2490",
        airline = "British Airways",
        departureAirport = "LHR",
        arrivalAirport = "EDI",
        scheduledDeparture = now + departureOffsetMs,
        scheduledArrival = now + arrivalOffsetMs,
        estimatedDeparture = null,
        estimatedArrival = null,
        status = status,
        gate = null,
        terminal = null,
        timestamp = now - fetchedAgoMs
    )

    @Test
    fun `never fetched flight is always polled`() {
        assertThat(FlightPollingPolicy.shouldPoll(null, now)).isTrue()
    }

    @Test
    fun `landed flight is never polled again`() {
        val landed = flight(status = "landed", fetchedAgoMs = TimeUnit.DAYS.toMillis(7))
        assertThat(FlightPollingPolicy.shouldPoll(landed, now)).isFalse()
    }

    @Test
    fun `cancelled and diverted flights are terminal too`() {
        listOf("cancelled", "diverted").forEach { status ->
            val f = flight(status = status, fetchedAgoMs = TimeUnit.DAYS.toMillis(1))
            assertThat(FlightPollingPolicy.shouldPoll(f, now)).isFalse()
        }
    }

    @Test
    fun `status match is case insensitive`() {
        // aviationstack returns lowercase, but the mapper capitalises it before caching.
        val f = flight(status = "Landed", fetchedAgoMs = TimeUnit.DAYS.toMillis(1))
        assertThat(FlightPollingPolicy.shouldPoll(f, now)).isFalse()
    }

    @Test
    fun `distant flight uses the idle interval`() {
        val f = flight(departureOffsetMs = TimeUnit.DAYS.toMillis(3))
        assertThat(FlightPollingPolicy.intervalFor(f, now))
            .isEqualTo(FlightPollingPolicy.IDLE_INTERVAL_MS)
    }

    @Test
    fun `distant flight fetched an hour ago is not re-polled`() {
        val f = flight(
            departureOffsetMs = TimeUnit.DAYS.toMillis(3),
            fetchedAgoMs = TimeUnit.HOURS.toMillis(1)
        )
        assertThat(FlightPollingPolicy.shouldPoll(f, now)).isFalse()
    }

    @Test
    fun `flight inside the departure window uses the active interval`() {
        val f = flight(departureOffsetMs = TimeUnit.HOURS.toMillis(1))
        assertThat(FlightPollingPolicy.intervalFor(f, now))
            .isEqualTo(FlightPollingPolicy.ACTIVE_INTERVAL_MS)
    }

    @Test
    fun `active flight is polled once the active interval has elapsed`() {
        val f = flight(
            departureOffsetMs = TimeUnit.HOURS.toMillis(1),
            fetchedAgoMs = FlightPollingPolicy.ACTIVE_INTERVAL_MS + 1
        )
        assertThat(FlightPollingPolicy.shouldPoll(f, now)).isTrue()
    }

    @Test
    fun `active flight polled a moment ago is not polled again`() {
        val f = flight(
            departureOffsetMs = TimeUnit.HOURS.toMillis(1),
            fetchedAgoMs = TimeUnit.MINUTES.toMillis(1)
        )
        assertThat(FlightPollingPolicy.shouldPoll(f, now)).isFalse()
    }

    @Test
    fun `window opens exactly three hours before departure`() {
        val justInside = flight(
            departureOffsetMs = FlightPollingPolicy.ACTIVE_WINDOW_BEFORE_MS - 1
        )
        val justOutside = flight(
            departureOffsetMs = FlightPollingPolicy.ACTIVE_WINDOW_BEFORE_MS + TimeUnit.MINUTES.toMillis(5)
        )
        assertThat(FlightPollingPolicy.intervalFor(justInside, now))
            .isEqualTo(FlightPollingPolicy.ACTIVE_INTERVAL_MS)
        assertThat(FlightPollingPolicy.intervalFor(justOutside, now))
            .isEqualTo(FlightPollingPolicy.IDLE_INTERVAL_MS)
    }

    @Test
    fun `in-flight aircraft still counts as active before arrival`() {
        val f = flight(
            departureOffsetMs = -TimeUnit.HOURS.toMillis(1),
            arrivalOffsetMs = TimeUnit.HOURS.toMillis(2)
        )
        assertThat(FlightPollingPolicy.intervalFor(f, now))
            .isEqualTo(FlightPollingPolicy.ACTIVE_INTERVAL_MS)
    }

    @Test
    fun `unknown schedule falls back to idle rather than polling hard`() {
        // Guards against burning quota on a flight we know nothing about.
        val f = flight().copy(scheduledDeparture = 0L, scheduledArrival = 0L)
        assertThat(FlightPollingPolicy.intervalFor(f, now))
            .isEqualTo(FlightPollingPolicy.IDLE_INTERVAL_MS)
    }

    @Test
    fun `flight is finished well after its arrival time`() {
        val f = flight(
            departureOffsetMs = -TimeUnit.DAYS.toMillis(1),
            arrivalOffsetMs = -TimeUnit.HOURS.toMillis(20)
        )
        assertThat(FlightPollingPolicy.isFinished(f, now)).isTrue()
    }

    @Test
    fun `flight just past arrival is not finished yet, to catch a late landing`() {
        val f = flight(
            departureOffsetMs = -TimeUnit.HOURS.toMillis(4),
            arrivalOffsetMs = -TimeUnit.MINUTES.toMillis(30)
        )
        assertThat(FlightPollingPolicy.isFinished(f, now)).isFalse()
    }

    @Test
    fun `a full flight lifecycle stays inside the free tier`() {
        // The whole point of the policy: simulate two days of ticks at the active interval
        // and assert the request count stays well under a 100/month allowance.
        var cached: FlightEntity? = null
        var requests = 0
        val departure = now + TimeUnit.DAYS.toMillis(1)
        val arrival = departure + TimeUnit.HOURS.toMillis(2)

        var clock = now
        val end = arrival + TimeUnit.HOURS.toMillis(6)
        val tick = TimeUnit.MINUTES.toMillis(5)

        while (clock < end) {
            if (FlightPollingPolicy.shouldPoll(cached, clock)) {
                requests++
                cached = FlightEntity(
                    flightNumber = "BA2490",
                    airline = "British Airways",
                    departureAirport = "LHR",
                    arrivalAirport = "EDI",
                    scheduledDeparture = departure,
                    scheduledArrival = arrival,
                    estimatedDeparture = null,
                    estimatedArrival = null,
                    status = if (clock > arrival) "landed" else "scheduled",
                    gate = null,
                    terminal = null,
                    timestamp = clock
                )
            }
            clock += tick
        }

        assertThat(requests).isLessThan(30)
    }
}
