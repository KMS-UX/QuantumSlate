package com.quantumslate.dashboard.data.remote.flight

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * aviationstack `GET /v1/flights`.
 *
 * Note the base URL is **http**, not https: aviationstack gates HTTPS behind its paid plans,
 * so the free tier cannot be reached over TLS. See `network_security_config.xml`, which
 * permits cleartext for this host only.
 */
interface AviationStackApiService {

    @GET("v1/flights")
    suspend fun getFlights(
        @Query("access_key") accessKey: String,
        @Query("flight_iata") flightIata: String,
        @Query("limit") limit: Int = 1
    ): AviationStackResponse

    companion object {
        const val BASE_URL = "http://api.aviationstack.com/"
        const val HOST = "api.aviationstack.com"
    }
}

data class AviationStackResponse(
    @SerializedName("data") val data: List<AviationStackFlight>? = null,
    @SerializedName("error") val error: AviationStackError? = null
)

data class AviationStackError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
)

data class AviationStackFlight(
    @SerializedName("flight_date") val flightDate: String? = null,
    @SerializedName("flight_status") val flightStatus: String? = null,
    @SerializedName("departure") val departure: AviationStackEndpoint? = null,
    @SerializedName("arrival") val arrival: AviationStackEndpoint? = null,
    @SerializedName("airline") val airline: AviationStackAirline? = null,
    @SerializedName("flight") val flight: AviationStackFlightInfo? = null
)

data class AviationStackEndpoint(
    @SerializedName("airport") val airport: String? = null,
    @SerializedName("iata") val iata: String? = null,
    @SerializedName("terminal") val terminal: String? = null,
    @SerializedName("gate") val gate: String? = null,
    @SerializedName("scheduled") val scheduled: String? = null,
    @SerializedName("estimated") val estimated: String? = null,
    @SerializedName("actual") val actual: String? = null
)

data class AviationStackAirline(
    @SerializedName("name") val name: String? = null,
    @SerializedName("iata") val iata: String? = null
)

data class AviationStackFlightInfo(
    @SerializedName("number") val number: String? = null,
    @SerializedName("iata") val iata: String? = null
)
