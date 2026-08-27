package com.quantumslate.dashboard.data.remote.flight

import com.quantumslate.dashboard.data.local.FlightEntity
import com.quantumslate.dashboard.di.FlightHttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [FlightDataSource] backed by aviationstack.
 *
 * Free tier: 100 requests/month, HTTP only. Both limits are declared on the interface so the
 * polling policy and the Settings screen can reason about them without special-casing.
 */
@Singleton
class AviationStackDataSource @Inject constructor(
    @FlightHttpClient private val okHttpClient: OkHttpClient
) : FlightDataSource {

    override val id: String = "aviationstack"
    override val displayName: String = "aviationstack"
    override val freeTierMonthlyRequests: Int = 100
    override val requiresCleartext: Boolean = true

    private val api: AviationStackApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AviationStackApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AviationStackApiService::class.java)
    }

    override suspend fun fetchFlight(
        flightNumber: String,
        apiKey: String
    ): Result<FlightEntity> {
        return try {
            val response = api.getFlights(accessKey = apiKey, flightIata = flightNumber)

            // aviationstack reports failures in a 200 body rather than an HTTP status.
            response.error?.let { err ->
                val message = err.message ?: "aviationstack error"
                return Result.failure(
                    when (err.code) {
                        "invalid_access_key", "missing_access_key", "inactive_user" ->
                            FlightAuthFailed(message)
                        "usage_limit_reached" -> FlightQuotaExhausted(message)
                        else -> Exception(message)
                    }
                )
            }

            val flight = response.data?.firstOrNull()
                ?: return Result.failure(FlightNotFound(flightNumber))

            Result.success(flight.toEntity(flightNumber))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * aviationstack timestamps are ISO-8601 with an offset, e.g. `2026-08-26T14:30:00+00:00`.
 * Returns 0 when absent or unparseable, which callers treat as "unknown".
 */
internal fun parseIso8601(value: String?): Long {
    if (value.isNullOrBlank()) return 0L
    // SimpleDateFormat's Z expects +0000, not +00:00, so drop the colon in the offset.
    val normalised = ISO_OFFSET_COLON.replace(value) { it.groupValues[1] + it.groupValues[2] }
    return try {
        ISO_FORMAT.get()!!.parse(normalised)?.time ?: 0L
    } catch (e: Exception) {
        try {
            ISO_NO_OFFSET.get()!!.parse(value)?.time ?: 0L
        } catch (e2: Exception) {
            0L
        }
    }
}

private val ISO_OFFSET_COLON = Regex("([+-]\\d{2}):(\\d{2})$")

private val ISO_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue() =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
}

private val ISO_NO_OFFSET = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue() =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
}

internal fun AviationStackFlight.toEntity(requestedNumber: String): FlightEntity {
    val scheduledDep = parseIso8601(departure?.scheduled)
    val scheduledArr = parseIso8601(arrival?.scheduled)

    return FlightEntity(
        flightNumber = flight?.iata?.uppercase() ?: requestedNumber.uppercase(),
        airline = airline?.name.orEmpty(),
        departureAirport = departure?.iata ?: departure?.airport.orEmpty(),
        arrivalAirport = arrival?.iata ?: arrival?.airport.orEmpty(),
        scheduledDeparture = scheduledDep,
        scheduledArrival = scheduledArr,
        // Prefer actual over estimated: once a flight has actually departed, the estimate is
        // stale and showing it would misreport a flight that is already in the air.
        estimatedDeparture = parseIso8601(departure?.actual ?: departure?.estimated)
            .takeIf { it > 0L },
        estimatedArrival = parseIso8601(arrival?.actual ?: arrival?.estimated)
            .takeIf { it > 0L },
        status = flightStatus?.replaceFirstChar { it.uppercase() } ?: "Unknown",
        gate = departure?.gate,
        terminal = departure?.terminal,
        timestamp = System.currentTimeMillis()
    )
}
