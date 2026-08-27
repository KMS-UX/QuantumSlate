package com.quantumslate.dashboard

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.quantumslate.dashboard.data.remote.flight.AviationStackResponse
import com.quantumslate.dashboard.data.remote.flight.parseIso8601
import com.quantumslate.dashboard.data.remote.flight.toEntity
import org.junit.Test

/**
 * The shipped flight code was written against AviationEdge's response shape while the
 * configured provider is aviationstack — it could never have parsed a real response. These
 * tests pin the replacement mapping to aviationstack's actual payload.
 */
class AviationStackMappingTest {

    private val gson = Gson()

    private val sample = """
    {
      "data": [{
        "flight_date": "2026-08-27",
        "flight_status": "active",
        "departure": {"airport": "Heathrow", "iata": "LHR", "terminal": "5", "gate": "A12",
                      "scheduled": "2026-08-27T14:30:00+00:00",
                      "estimated": "2026-08-27T14:45:00+00:00"},
        "arrival":   {"airport": "Edinburgh", "iata": "EDI", "terminal": "1", "gate": null,
                      "scheduled": "2026-08-27T15:50:00+00:00"},
        "airline": {"name": "British Airways", "iata": "BA"},
        "flight": {"number": "2490", "iata": "BA2490"}
      }]
    }
    """.trimIndent()

    private fun firstFlight() =
        gson.fromJson(sample, AviationStackResponse::class.java).data!!.first()

    @Test
    fun `maps the core fields`() {
        val e = firstFlight().toEntity("ba2490")
        assertThat(e.flightNumber).isEqualTo("BA2490")
        assertThat(e.airline).isEqualTo("British Airways")
        assertThat(e.departureAirport).isEqualTo("LHR")
        assertThat(e.arrivalAirport).isEqualTo("EDI")
        assertThat(e.gate).isEqualTo("A12")
        assertThat(e.terminal).isEqualTo("5")
    }

    @Test
    fun `capitalises the status for display`() {
        assertThat(firstFlight().toEntity("BA2490").status).isEqualTo("Active")
    }

    @Test
    fun `falls back to the requested number when the payload omits it`() {
        val r = gson.fromJson(
            """{"data":[{"flight_status":"scheduled"}]}""",
            AviationStackResponse::class.java
        )
        assertThat(r.data!!.first().toEntity("u21234").flightNumber).isEqualTo("U21234")
    }

    @Test
    fun `prefers actual over estimated once a flight has really departed`() {
        // Showing a stale estimate for an aircraft already in the air misreports the flight.
        val json = """
        {"data":[{"departure":{"scheduled":"2026-08-27T14:30:00+00:00",
                               "estimated":"2026-08-27T14:45:00+00:00",
                               "actual":"2026-08-27T14:52:00+00:00"}}]}
        """.trimIndent()
        val e = gson.fromJson(json, AviationStackResponse::class.java).data!!.first().toEntity("X1")
        assertThat(e.estimatedDeparture).isEqualTo(parseIso8601("2026-08-27T14:52:00+00:00"))
    }

    @Test
    fun `absent times become null rather than epoch zero`() {
        val r = gson.fromJson("""{"data":[{"flight_status":"scheduled"}]}""", AviationStackResponse::class.java)
        val e = r.data!!.first().toEntity("X1")
        assertThat(e.estimatedDeparture).isNull()
        assertThat(e.estimatedArrival).isNull()
        assertThat(e.scheduledDeparture).isEqualTo(0L)
    }

    @Test
    fun `parses the colon-offset ISO format aviationstack actually returns`() {
        // SimpleDateFormat's Z wants +0000, not +00:00 — the mapper normalises it.
        assertThat(parseIso8601("2026-08-27T14:30:00+00:00")).isGreaterThan(0L)
        assertThat(parseIso8601("2026-08-27T14:30:00+02:00")).isGreaterThan(0L)
    }

    @Test
    fun `offset is honoured, not ignored`() {
        val utc = parseIso8601("2026-08-27T14:30:00+00:00")
        val plusTwo = parseIso8601("2026-08-27T14:30:00+02:00")
        assertThat(utc - plusTwo).isEqualTo(2 * 60 * 60 * 1000L)
    }

    @Test
    fun `unparseable and blank times return zero instead of throwing`() {
        assertThat(parseIso8601(null)).isEqualTo(0L)
        assertThat(parseIso8601("")).isEqualTo(0L)
        assertThat(parseIso8601("not a date")).isEqualTo(0L)
    }

    @Test
    fun `error payloads are surfaced rather than parsed as data`() {
        val r = gson.fromJson(
            """{"error":{"code":"usage_limit_reached","message":"quota"}}""",
            AviationStackResponse::class.java
        )
        assertThat(r.error).isNotNull()
        assertThat(r.error!!.code).isEqualTo("usage_limit_reached")
        assertThat(r.data).isNull()
    }
}
