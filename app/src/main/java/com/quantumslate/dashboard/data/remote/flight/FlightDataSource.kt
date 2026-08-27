package com.quantumslate.dashboard.data.remote.flight

import com.quantumslate.dashboard.data.local.FlightEntity

/**
 * A provider of flight status data.
 *
 * Flight APIs differ sharply in response shape, auth style, and free-tier limits, and the
 * one we start on (aviationstack) allows only 100 requests a month. Keeping providers behind
 * this interface means switching to a more generous one later is a configuration change and
 * a new implementation file — not a rewrite of the repository, the widget, or the scheduler.
 */
interface FlightDataSource {

    /** Stable identifier persisted in preferences to select this provider. */
    val id: String

    /** Human-readable name, shown in Settings. */
    val displayName: String

    /**
     * Requests published for this provider's free tier, or `null` if it does not publish one.
     * Used by the request budget to warn before the allowance is exhausted.
     */
    val freeTierMonthlyRequests: Int?

    /**
     * True when this provider requires cleartext HTTP on its free tier.
     *
     * Surfaced rather than hidden, because it forces a network-security-config exception
     * and is a genuine (if provider-imposed) weakening of Bible §12.
     */
    val requiresCleartext: Boolean get() = false

    /**
     * Fetches current status for [flightNumber] (IATA form, e.g. "BA2490").
     *
     * Implementations must return [FlightNotFound] rather than a generic failure when the
     * provider responds successfully but has no record of the flight, so the caller can tell
     * "bad flight number" apart from "provider unreachable" and avoid wasting retries.
     */
    suspend fun fetchFlight(flightNumber: String, apiKey: String): Result<FlightEntity>
}

/** The provider answered, but knows nothing about this flight number. Retrying will not help. */
class FlightNotFound(flightNumber: String) :
    Exception("No flight data found for $flightNumber")

/** The provider rejected the credentials. Retrying will not help until the key is fixed. */
class FlightAuthFailed(message: String) : Exception(message)

/** The caller has exhausted its request allowance for this provider. */
class FlightQuotaExhausted(message: String) : Exception(message)
