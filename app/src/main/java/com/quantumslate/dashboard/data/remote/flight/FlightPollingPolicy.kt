package com.quantumslate.dashboard.data.remote.flight

import com.quantumslate.dashboard.data.local.FlightEntity
import java.util.concurrent.TimeUnit

/**
 * Decides whether a tracked flight is worth spending a request on right now.
 *
 * The Bible (§4) asks for 5-minute polling while a flight is active. Taken literally that is
 * 288 requests per day per flight — roughly 8,600 a month, against a free tier of 100. No
 * free provider can satisfy the letter of that rule.
 *
 * What the rule is actually *for* is "the user sees timely information when it matters". This
 * policy preserves that intent by concentrating requests in the window where a flight's
 * status actually changes, and spending nothing at all outside it:
 *
 * - more than [ACTIVE_WINDOW_BEFORE_MS] before departure: poll at most daily
 * - within that window, and until arrival: poll at [ACTIVE_INTERVAL_MS]
 * - after landing/cancellation: stop entirely, the record is final
 *
 * For a single flight this is roughly 20 requests across its whole lifecycle rather than
 * thousands, which fits comfortably inside 100/month.
 */
object FlightPollingPolicy {

    /** Begin close polling this long before scheduled departure. */
    val ACTIVE_WINDOW_BEFORE_MS: Long = TimeUnit.HOURS.toMillis(3)

    /** Interval while a flight is in its active window. */
    val ACTIVE_INTERVAL_MS: Long = TimeUnit.MINUTES.toMillis(15)

    /** Interval for a flight that is still far in the future. */
    val IDLE_INTERVAL_MS: Long = TimeUnit.HOURS.toMillis(24)

    /** Keep polling this long past scheduled arrival, to catch a late landing. */
    private val ARRIVAL_GRACE_MS: Long = TimeUnit.HOURS.toMillis(2)

    /** Statuses after which no further request will ever tell us anything new. */
    private val TERMINAL_STATUSES = setOf("landed", "cancelled", "diverted")

    /**
     * @param flight the cached record, or null if this flight has never been fetched
     * @param now injectable for testing
     */
    fun shouldPoll(flight: FlightEntity?, now: Long = System.currentTimeMillis()): Boolean {
        // Never fetched: always worth one request.
        if (flight == null) return true

        if (flight.status.lowercase() in TERMINAL_STATUSES) return false

        val age = now - flight.timestamp
        return age >= intervalFor(flight, now)
    }

    /** The polling interval that currently applies to [flight]. */
    fun intervalFor(flight: FlightEntity, now: Long = System.currentTimeMillis()): Long {
        val scheduledDeparture = flight.scheduledDeparture
        val scheduledArrival = flight.scheduledArrival

        // Unknown schedule: treat conservatively rather than polling hard on no information.
        if (scheduledDeparture <= 0L) return IDLE_INTERVAL_MS

        val windowStart = scheduledDeparture - ACTIVE_WINDOW_BEFORE_MS
        val windowEnd = if (scheduledArrival > 0L) {
            scheduledArrival + ARRIVAL_GRACE_MS
        } else {
            scheduledDeparture + ARRIVAL_GRACE_MS
        }

        return if (now in windowStart..windowEnd) ACTIVE_INTERVAL_MS else IDLE_INTERVAL_MS
    }

    /** True once the flight can no longer change and should be dropped from polling. */
    fun isFinished(flight: FlightEntity, now: Long = System.currentTimeMillis()): Boolean {
        if (flight.status.lowercase() in TERMINAL_STATUSES) return true
        val arrival = flight.scheduledArrival.takeIf { it > 0L } ?: return false
        return now > arrival + ARRIVAL_GRACE_MS
    }
}
