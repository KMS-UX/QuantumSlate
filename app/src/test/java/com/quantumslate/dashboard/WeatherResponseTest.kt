package com.quantumslate.dashboard

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.quantumslate.dashboard.data.remote.WeatherResponse
import org.junit.Test

/**
 * `WeatherResponse` did not exist until this repair — `WeatherApi` referenced a type that was
 * never written, so the weather path could not compile, let alone run. These tests pin the
 * mapping against a real OpenWeatherMap payload shape.
 */
class WeatherResponseTest {

    private val gson = Gson()

    private val sample = """
        {
          "weather": [{"id": 500, "main": "Rain", "description": "light rain", "icon": "10d"}],
          "main": {"temp": 14.2, "feels_like": 13.4, "temp_min": 11.0, "temp_max": 16.5,
                   "pressure": 1012, "humidity": 82},
          "wind": {"speed": 4.6, "deg": 210},
          "dt": 1724680800,
          "name": "Edinburgh"
        }
    """.trimIndent()

    @Test
    fun `parses a full payload`() {
        val r = gson.fromJson(sample, WeatherResponse::class.java)
        val w = r.toDomainModel()

        assertThat(w.temperature).isWithin(0.001).of(14.2)
        assertThat(w.location).isEqualTo("Edinburgh")
        assertThat(w.highTemp).isWithin(0.001).of(16.5)
        assertThat(w.lowTemp).isWithin(0.001).of(11.0)
        assertThat(w.humidity).isEqualTo(82)
        assertThat(w.windSpeed).isWithin(0.001).of(4.6)
    }

    @Test
    fun `prefers the human readable description over the condition group`() {
        val w = gson.fromJson(sample, WeatherResponse::class.java).toDomainModel()
        assertThat(w.condition).isEqualTo("light rain")
    }

    @Test
    fun `converts dt from seconds to milliseconds`() {
        // OpenWeatherMap reports seconds; the rest of the app works in millis. Getting this
        // wrong would place every reading in 1970 and mark the cache permanently expired.
        val w = gson.fromJson(sample, WeatherResponse::class.java).toDomainModel()
        assertThat(w.timestamp).isEqualTo(1724680800L * 1000L)
    }

    @Test
    fun `builds an icon url from the icon code`() {
        val w = gson.fromJson(sample, WeatherResponse::class.java).toDomainModel()
        assertThat(w.iconUrl).isEqualTo("https://openweathermap.org/img/wn/10d@2x.png")
    }

    @Test
    fun `missing weather array does not crash`() {
        val r = gson.fromJson("""{"main":{"temp":9.0},"name":"Nowhere"}""", WeatherResponse::class.java)
        val w = r.toDomainModel()
        assertThat(w.condition).isEqualTo("Unknown")
        assertThat(w.iconUrl).isNull()
        assertThat(w.temperature).isWithin(0.001).of(9.0)
    }

    @Test
    fun `absent dt falls back to now rather than epoch zero`() {
        val before = System.currentTimeMillis()
        val w = gson.fromJson("""{"name":"X"}""", WeatherResponse::class.java).toDomainModel()
        assertThat(w.timestamp).isAtLeast(before)
    }

    @Test
    fun `missing high and low fall back to the current temperature`() {
        val r = gson.fromJson("""{"main":{"temp":21.0},"name":"X"}""", WeatherResponse::class.java)
        val w = r.toDomainModel()
        assertThat(w.highTemp).isWithin(0.001).of(21.0)
        assertThat(w.lowTemp).isWithin(0.001).of(21.0)
    }

    @Test
    fun `empty payload yields a usable object`() {
        val w = gson.fromJson("{}", WeatherResponse::class.java).toDomainModel()
        assertThat(w.location).isEmpty()
        assertThat(w.humidity).isEqualTo(0)
    }
}
