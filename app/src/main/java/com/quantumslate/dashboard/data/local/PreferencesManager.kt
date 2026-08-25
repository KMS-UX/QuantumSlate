package com.quantumslate.dashboard.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
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

    fun getFlightApiApiKey(): String? = encryptedPrefs.getString("flight_api_key", null)
    fun saveFlightApiApiKey(key: String) = encryptedPrefs.edit().putString("flight_api_key", key).apply()

    fun getSpotifyClientId(): String? = encryptedPrefs.getString("spotify_client_id", null)
    fun saveSpotifyClientId(id: String) = encryptedPrefs.edit().putString("spotify_client_id", id).apply()

    fun getSpotifyClientSecret(): String? = encryptedPrefs.getString("spotify_client_secret", null)
    fun saveSpotifyClientSecret(secret: String) = encryptedPrefs.edit().putString("spotify_client_secret", secret).apply()

    fun getSpotifyRefreshToken(): String? = encryptedPrefs.getString("spotify_refresh_token", null)
    fun saveSpotifyRefreshToken(token: String) = encryptedPrefs.edit().putString("spotify_refresh_token", token).apply()

    // User Settings (stored in regular prefs)
    fun getUpdateMode(): UpdateMode = UpdateMode.valueOf(regularPrefs.getString("update_mode", UpdateMode.DAILY.name) ?: UpdateMode.DAILY.name)
    fun saveUpdateMode(mode: UpdateMode) = regularPrefs.edit().putString("update_mode", mode.name).apply()

    fun getAutoUpdateTime(): String = regularPrefs.getString("auto_update_time", "08:00") ?: "08:00"
    fun saveAutoUpdateTime(time: String) = regularPrefs.edit().putString("auto_update_time", time).apply()

    fun getLocationEnabled(): Boolean = regularPrefs.getBoolean("location_enabled", true)
    fun saveLocationEnabled(enabled: Boolean) = regularPrefs.edit().putBoolean("location_enabled", enabled).apply()

    fun getDarkMode(): DarkMode = DarkMode.valueOf(regularPrefs.getString("dark_mode", DarkMode.AUTO.name) ?: DarkMode.AUTO.name)
    fun saveDarkMode(mode: DarkMode) = regularPrefs.edit().putString("dark_mode", mode.name).apply()

    fun getDefaultUiMode(): UiMode = UiMode.valueOf(regularPrefs.getString("default_ui_mode", UiMode.MINIMALIST.name) ?: UiMode.MINIMALIST.name)
    fun saveDefaultUiMode(mode: UiMode) = regularPrefs.edit().putString("default_ui_mode", mode.name).apply()

    fun getMascotCharacter(): String = regularPrefs.getString("mascot_character", "robot") ?: "robot"
    fun saveMascotCharacter(character: String) = regularPrefs.edit().putString("mascot_character", character).apply()

    fun getMascotAnimationsEnabled(): Boolean = regularPrefs.getBoolean("mascot_animations_enabled", true)
    fun saveMascotAnimationsEnabled(enabled: Boolean) = regularPrefs.edit().putBoolean("mascot_animations_enabled", enabled).apply()

    fun getWidgetOrder(): List<String> {
        val order = regularPrefs.getString("widget_order", "TIME,WEATHER,CALENDAR,RSS,FLIGHT,SPOTIFY,MASCOT") ?: "TIME,WEATHER,CALENDAR,RSS,FLIGHT,SPOTIFY,MASCOT"
        return order.split(",")
    }

    fun saveWidgetOrder(order: List<String>) = regularPrefs.edit().putString("widget_order", order.joinToString(",")).apply()

    fun getEnabledWidgets(): Set<String> = regularPrefs.getStringSet("enabled_widgets", setOf("TIME", "WEATHER", "CALENDAR")) ?: setOf("TIME", "WEATHER", "CALENDAR")
    fun saveEnabledWidgets(widgets: Set<String>) = regularPrefs.edit().putStringSet("enabled_widgets", widgets).apply()

    fun getCustomRssFeeds(): List<String> {
        val feeds = regularPrefs.getString("custom_rss_feeds", "") ?: ""
        return if (feeds.isEmpty()) emptyList() else feeds.split("|")
    }

    fun saveCustomRssFeeds(feeds: List<String>) = regularPrefs.edit().putString("custom_rss_feeds", feeds.joinToString("|")).apply()

    fun getTrackedFlights(): List<String> {
        val flights = regularPrefs.getString("tracked_flights", "") ?: ""
        return if (flights.isEmpty()) emptyList() else flights.split("|")
    }

    fun saveTrackedFlights(flights: List<String>) = regularPrefs.edit().putString("tracked_flights", flights.joinToString("|")).apply()

    fun getLastUpdateTime(): Long = regularPrefs.getLong("last_update_time", 0L)
    fun saveLastUpdateTime(time: Long) = regularPrefs.edit().putLong("last_update_time", time).apply()
    
    // Additional methods for Phase 2
    fun getFlightApiKey(): String? = encryptedPrefs.getString("flight_api_key", null)
    fun saveFlightApiKey(key: String) = encryptedPrefs.edit().putString("flight_api_key", key).apply()
    
    fun getSpotifyAccessToken(): String? = encryptedPrefs.getString("spotify_access_token", null)
    fun saveSpotifyAccessToken(token: String) = encryptedPrefs.edit().putString("spotify_access_token", token).apply()
    
    fun isSpotifyEnabled(): Boolean = regularPrefs.getBoolean("spotify_enabled", false)
    fun saveSpotifyEnabled(enabled: Boolean) = regularPrefs.edit().putBoolean("spotify_enabled", enabled).apply()
    
    fun getRssFeeds(): List<String> {
        val feeds = regularPrefs.getString("rss_feeds", "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml") ?: ""
        return if (feeds.isEmpty()) emptyList() else feeds.split("|")
    }
    
    fun saveRssFeeds(feeds: List<String>) = regularPrefs.edit().putString("rss_feeds", feeds.joinToString("|")).apply()
    
    fun getLatitude(): Double? = regularPrefs.getFloat("latitude", Float.NaN).takeIf { !it.isNaN() }?.toDouble()
    fun saveLatitude(lat: Double) = regularPrefs.edit().putFloat("latitude", lat.toFloat()).apply()
    
    fun getLongitude(): Double? = regularPrefs.getFloat("longitude", Float.NaN).takeIf { !it.isNaN() }?.toDouble()
    fun saveLongitude(lon: Double) = regularPrefs.edit().putFloat("longitude", lon.toFloat()).apply()
    
    fun getLocation(): String? = regularPrefs.getString("location_name", null)
    fun saveLocation(name: String) = regularPrefs.edit().putString("location_name", name).apply()
    
    fun getUpdateFrequency(): String? = regularPrefs.getString("update_frequency", "daily")
    fun saveUpdateFrequency(frequency: String) = regularPrefs.edit().putString("update_frequency", frequency).apply()
    
    fun areMascotAnimationsEnabled(): Boolean = regularPrefs.getBoolean("mascot_animations_enabled", true)

    enum class UpdateMode { DAILY, AMBIENT, REAL_TIME }
    enum class DarkMode { LIGHT, DARK, AUTO }
    enum class UiMode { MINIMALIST, DATA_DENSE, RETRO }
}
