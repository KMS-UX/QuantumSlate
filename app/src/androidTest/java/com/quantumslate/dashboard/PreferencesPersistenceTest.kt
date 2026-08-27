package com.quantumslate.dashboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.quantumslate.dashboard.data.local.DashboardWidget
import com.quantumslate.dashboard.data.local.PreferencesManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Persistence tests that cannot run on the JVM: `EncryptedSharedPreferences` needs the
 * Android keystore, and the whole class of bug this suite guards against — a setting saved
 * under one key and read under another — is invisible to a unit test with a fake.
 *
 * This is the bug that shipped: Settings wrote `update_mode` while the scheduler read
 * `update_frequency`, so changing update frequency did nothing at all.
 */
@RunWith(AndroidJUnit4::class)
class PreferencesPersistenceTest {

    private lateinit var prefs: PreferencesManager

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        prefs = PreferencesManager(context)
    }

    @Test
    fun apiKeysRoundTripThroughEncryptedStorage() {
        prefs.saveOpenWeatherApiKey("weather-key-123")
        prefs.saveFlightApiKey("flight-key-456")
        prefs.saveSpotifyClientId("spotify-client-789")

        assertThat(prefs.getOpenWeatherApiKey()).isEqualTo("weather-key-123")
        assertThat(prefs.getFlightApiKey()).isEqualTo("flight-key-456")
        assertThat(prefs.getSpotifyClientId()).isEqualTo("spotify-client-789")
    }

    @Test
    fun updateModeReadsBackWhatSettingsWrote() {
        // The exact bug that shipped: two accessors, two different keys.
        PreferencesManager.UpdateMode.entries.forEach { mode ->
            prefs.saveUpdateMode(mode)
            assertThat(prefs.getUpdateMode()).isEqualTo(mode)
        }
    }

    @Test
    fun uiModeRoundTripsIncludingTheFourthMode() {
        PreferencesManager.UiMode.entries.forEach { mode ->
            prefs.saveDefaultUiMode(mode)
            assertThat(prefs.getDefaultUiMode()).isEqualTo(mode)
        }
    }

    @Test
    fun darkModeRoundTrips() {
        PreferencesManager.DarkMode.entries.forEach { mode ->
            prefs.saveDarkMode(mode)
            assertThat(prefs.getDarkMode()).isEqualTo(mode)
        }
    }

    @Test
    fun widgetLayoutSurvivesAWriteAndReread() {
        val layout = prefs.getWidgetLayout()
            .toggled(DashboardWidget.NEWS)
            .moved(DashboardWidget.MASCOT, up = true)

        prefs.saveWidgetLayout(layout)
        val restored = prefs.getWidgetLayout()

        assertThat(restored.isEnabled(DashboardWidget.NEWS)).isFalse()
        assertThat(restored.order).isEqualTo(layout.order)
    }

    @Test
    fun trackedFlightsRoundTripAndDoNotSplitOnEmpty() {
        prefs.saveTrackedFlights(emptyList())
        assertThat(prefs.getTrackedFlights()).isEmpty()

        prefs.saveTrackedFlights(listOf("BA2490", "U21234"))
        assertThat(prefs.getTrackedFlights()).containsExactly("BA2490", "U21234").inOrder()
    }

    @Test
    fun rssFeedsRoundTrip() {
        prefs.saveRssFeeds(listOf("https://a.example/feed.xml", "https://b.example/atom.xml"))
        assertThat(prefs.getRssFeeds()).hasSize(2)
    }

    @Test
    fun clearingSpotifyTokensRemovesAllThree() {
        prefs.saveSpotifyAccessToken("access")
        prefs.saveSpotifyRefreshToken("refresh")
        prefs.saveSpotifyTokenExpiry(System.currentTimeMillis() + 3_600_000)

        prefs.clearSpotifyTokens()

        assertThat(prefs.getSpotifyAccessToken()).isNull()
        assertThat(prefs.getSpotifyRefreshToken()).isNull()
        assertThat(prefs.getSpotifyTokenExpiry()).isEqualTo(0L)
    }

    @Test
    fun locationToggleRoundTrips() {
        // This setting was stored but never read for five phases; pin it.
        prefs.saveLocationEnabled(false)
        assertThat(prefs.getLocationEnabled()).isFalse()
        prefs.saveLocationEnabled(true)
        assertThat(prefs.getLocationEnabled()).isTrue()
    }
}
