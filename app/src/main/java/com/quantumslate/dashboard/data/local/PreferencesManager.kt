package com.quantumslate.dashboard.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val encryptedPrefs: EncryptedSharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "quantumslate_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    private val regularPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("quantumslate_regular_prefs", Context.MODE_PRIVATE)
    }

    // API Keys (stored encrypted)
    fun getOpenWeatherApiKey(): String? = encryptedPrefs.getString("openweather_api_key", null)
    fun saveOpenWeatherApiKey(key: String) = encryptedPrefs.edit().putString("openweather_api_key", key).apply()


    fun getSpotifyClientId(): String? = encryptedPrefs.getString("spotify_client_id", null)
    fun saveSpotifyClientId(id: String) = encryptedPrefs.edit().putString("spotify_client_id", id).apply()


    fun getSpotifyRefreshToken(): String? = encryptedPrefs.getString("spotify_refresh_token", null)
    fun saveSpotifyRefreshToken(token: String) = encryptedPrefs.edit().putString("spotify_refresh_token", token).apply()

    // User Settings (stored in regular prefs)
    /**
     * Enum settings are parsed defensively.
     *
     * `valueOf` throws on any unrecognised string, and these values come from persisted user
     * data — a value written by an older or newer build, or a partially-migrated install, is
     * enough to crash the screen that reads it rather than degrade to a default.
     */
    fun getUpdateMode(): UpdateMode =
        enumOrDefault(regularPrefs.getString("update_mode", null), UpdateMode.DAILY)
    fun saveUpdateMode(mode: UpdateMode) = regularPrefs.edit().putString("update_mode", mode.name).apply()

    fun getAutoUpdateTime(): String = regularPrefs.getString("auto_update_time", "08:00") ?: "08:00"
    fun saveAutoUpdateTime(time: String) = regularPrefs.edit().putString("auto_update_time", time).apply()

    fun getLocationEnabled(): Boolean = regularPrefs.getBoolean("location_enabled", true)
    fun saveLocationEnabled(enabled: Boolean) = regularPrefs.edit().putBoolean("location_enabled", enabled).apply()

    fun getDarkMode(): DarkMode =
        enumOrDefault(regularPrefs.getString("dark_mode", null), DarkMode.AUTO)
    fun saveDarkMode(mode: DarkMode) = regularPrefs.edit().putString("dark_mode", mode.name).apply()

    fun getDefaultUiMode(): UiMode =
        enumOrDefault(regularPrefs.getString("default_ui_mode", null), UiMode.default)
            .takeIf { it.isShipping } ?: UiMode.default
    fun saveDefaultUiMode(mode: UiMode) = regularPrefs.edit().putString("default_ui_mode", mode.name).apply()

    fun getMascotCharacter(): String = regularPrefs.getString("mascot_character", QUANTUM_BOY) ?: QUANTUM_BOY
    /** The only shipped mascot; kept as a setting for future characters. */
    fun saveMascotCharacter(character: String) = regularPrefs.edit().putString("mascot_character", character).apply()

    fun saveMascotAnimationsEnabled(enabled: Boolean) = regularPrefs.edit().putBoolean("mascot_animations_enabled", enabled).apply()

    // ==================== Flight API ====================

    fun getFlightApiKey(): String? = encryptedPrefs.getString("flight_api_key", null)
    fun saveFlightApiKey(key: String) = encryptedPrefs.edit().putString("flight_api_key", key).apply()

    // ==================== Spotify tokens ====================
    //
    // No client secret is stored: the app uses Authorization Code + PKCE precisely because a
    // mobile client cannot keep one.

    fun getSpotifyAccessToken(): String? = encryptedPrefs.getString("spotify_access_token", null)
    fun saveSpotifyAccessToken(token: String) = encryptedPrefs.edit().putString("spotify_access_token", token).apply()

    /** Epoch millis at which the stored access token stops being valid. */
    fun getSpotifyTokenExpiry(): Long = encryptedPrefs.getLong("spotify_token_expiry", 0L)
    fun saveSpotifyTokenExpiry(expiryMillis: Long) = encryptedPrefs.edit().putLong("spotify_token_expiry", expiryMillis).apply()

    /** Clears every Spotify credential; used when the user disconnects or auth is revoked. */
    fun clearSpotifyTokens() = encryptedPrefs.edit()
        .remove("spotify_access_token")
        .remove("spotify_refresh_token")
        .remove("spotify_token_expiry")
        .apply()

    fun isSpotifyEnabled(): Boolean = regularPrefs.getBoolean("spotify_enabled", false)
    fun saveSpotifyEnabled(enabled: Boolean) = regularPrefs.edit().putBoolean("spotify_enabled", enabled).apply()

    // ==================== Tracked flights ====================

    fun getTrackedFlights(): List<String> {
        val flights = regularPrefs.getString("tracked_flights", "") ?: ""
        return if (flights.isEmpty()) emptyList() else flights.split("|")
    }

    fun saveTrackedFlights(flights: List<String>) =
        regularPrefs.edit().putString("tracked_flights", flights.joinToString("|")).apply()

    // Legacy widget-order / enabled-widgets / custom-feed accessors were removed: they
    // were superseded by getWidgetLayout() and, worse, getWidgetOrder() used the same
    // "widget_order" key with a different separator, so the two could have silently
    // corrupted each other's data.

    fun getRssFeeds(): List<String> {
        val feeds = regularPrefs.getString("rss_feeds", "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml") ?: ""
        return if (feeds.isEmpty()) emptyList() else feeds.split("|")
    }
    
    fun saveRssFeeds(feeds: List<String>) = regularPrefs.edit().putString("rss_feeds", feeds.joinToString("|")).apply()
    
    fun getLocation(): String? = regularPrefs.getString("location_name", null)
    fun saveLocation(name: String) = regularPrefs.edit().putString("location_name", name).apply()
    
    
    fun areMascotAnimationsEnabled(): Boolean = regularPrefs.getBoolean("mascot_animations_enabled", true)

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T =
        raw?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: fallback

    enum class UpdateMode { DAILY, AMBIENT, REAL_TIME }
    enum class DarkMode { LIGHT, DARK, AUTO }
    // ==================== Widget layout (Bible §5) ====================

    /**
     * Persisted as two delimited strings rather than a serialised object, matching how the
     * rest of this class stores lists and keeping it readable in `adb shell dumpsys`.
     */
    fun getWidgetLayout(): WidgetLayout {
        val orderRaw = regularPrefs.getString("widget_order", null)
        val enabledRaw = regularPrefs.getString("widget_enabled", null)

        val order = orderRaw?.split("|")?.mapNotNull { DashboardWidget.fromKey(it) }
            ?.takeIf { it.isNotEmpty() }
            // Append any widget added in a later app version so it is never silently missing.
            ?.let { stored -> stored + DashboardWidget.entries.filterNot { it in stored } }
            ?: DashboardWidget.entries.toList()

        val enabled = enabledRaw?.split("|")?.mapNotNull { DashboardWidget.fromKey(it) }?.toSet()
            ?: DashboardWidget.entries.toSet()

        return WidgetLayout(enabled = enabled, order = order)
    }

    fun saveWidgetLayout(layout: WidgetLayout) = regularPrefs.edit()
        .putString("widget_order", layout.order.joinToString("|") { it.key })
        .putString("widget_enabled", layout.enabled.joinToString("|") { it.key })
        .apply()

    companion object { const val QUANTUM_BOY = "quantum_boy" }

    enum class UiMode(val displayName: String, val isShipping: Boolean) {
        /** Retired 2026-08-27; kept so the unreferenced dashboards still compile. */
        MINIMALIST("Minimalist", isShipping = false),

        /** Retired 2026-08-27. */
        DATA_DENSE("Data Dense", isShipping = false),

        QUANTUM_DAILY("Quantum Daily", isShipping = true),
        QUANTUM_EFFECT("QuantumEffect", isShipping = true);

        companion object {
            /** The modes the app actually navigates between. */
            val shipping: List<UiMode> get() = entries.filter { it.isShipping }

            val default: UiMode get() = QUANTUM_DAILY
        }
    }
}
