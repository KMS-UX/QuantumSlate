package com.quantumslate.dashboard.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.remote.spotify.SpotifyAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val openWeatherApiKey: String? = null,
    val flightApiKey: String? = null,
    val updateMode: PreferencesManager.UpdateMode = PreferencesManager.UpdateMode.DAILY,
    val autoUpdateTime: String = "08:00",
    val locationEnabled: Boolean = true,
    val darkMode: PreferencesManager.DarkMode = PreferencesManager.DarkMode.AUTO,
    val defaultUiMode: PreferencesManager.UiMode = PreferencesManager.UiMode.MINIMALIST,
    val mascotCharacter: String = "robot",
    val mascotAnimationsEnabled: Boolean = true,
    val spotifyClientId: String? = null,
    val spotifyConnected: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val spotifyAuthManager: SpotifyAuthManager
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _settingsState.value = SettingsUiState(
            openWeatherApiKey = preferencesManager.getOpenWeatherApiKey(),
            flightApiKey = preferencesManager.getFlightApiApiKey(),
            updateMode = preferencesManager.getUpdateMode(),
            autoUpdateTime = preferencesManager.getAutoUpdateTime(),
            locationEnabled = preferencesManager.getLocationEnabled(),
            darkMode = preferencesManager.getDarkMode(),
            defaultUiMode = preferencesManager.getDefaultUiMode(),
            mascotCharacter = preferencesManager.getMascotCharacter(),
            mascotAnimationsEnabled = preferencesManager.getMascotAnimationsEnabled(),
            spotifyClientId = preferencesManager.getSpotifyClientId(),
            spotifyConnected = spotifyAuthManager.isConnected
        )
    }

    fun saveOpenWeatherApiKey(key: String) {
        viewModelScope.launch {
            preferencesManager.saveOpenWeatherApiKey(key)
            _settingsState.value = _settingsState.value.copy(openWeatherApiKey = key)
        }
    }

    fun saveFlightApiKey(key: String) {
        viewModelScope.launch {
            preferencesManager.saveFlightApiApiKey(key)
            _settingsState.value = _settingsState.value.copy(flightApiKey = key)
        }
    }

    fun saveUpdateMode(mode: PreferencesManager.UpdateMode) {
        viewModelScope.launch {
            preferencesManager.saveUpdateMode(mode)
            _settingsState.value = _settingsState.value.copy(updateMode = mode)
        }
    }

    fun saveAutoUpdateTime(time: String) {
        viewModelScope.launch {
            preferencesManager.saveAutoUpdateTime(time)
            _settingsState.value = _settingsState.value.copy(autoUpdateTime = time)
        }
    }

    fun saveLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveLocationEnabled(enabled)
            _settingsState.value = _settingsState.value.copy(locationEnabled = enabled)
        }
    }

    fun saveDarkMode(mode: PreferencesManager.DarkMode) {
        viewModelScope.launch {
            preferencesManager.saveDarkMode(mode)
            _settingsState.value = _settingsState.value.copy(darkMode = mode)
        }
    }

    fun saveDefaultUiMode(mode: PreferencesManager.UiMode) {
        viewModelScope.launch {
            preferencesManager.saveDefaultUiMode(mode)
            _settingsState.value = _settingsState.value.copy(defaultUiMode = mode)
        }
    }

    fun saveMascotCharacter(character: String) {
        viewModelScope.launch {
            preferencesManager.saveMascotCharacter(character)
            _settingsState.value = _settingsState.value.copy(mascotCharacter = character)
        }
    }

    fun saveMascotAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveMascotAnimationsEnabled(enabled)
            _settingsState.value = _settingsState.value.copy(mascotAnimationsEnabled = enabled)
        }
    }

    // ==================== SPOTIFY ====================

    fun saveSpotifyClientId(clientId: String) {
        viewModelScope.launch {
            preferencesManager.saveSpotifyClientId(clientId)
            _settingsState.value = _settingsState.value.copy(spotifyClientId = clientId)
        }
    }

    /** Opens the Spotify consent page. The result arrives via SpotifyRedirectActivity. */
    fun connectSpotify(): Result<Unit> = spotifyAuthManager.beginAuthorization()

    fun disconnectSpotify() {
        viewModelScope.launch {
            spotifyAuthManager.disconnect()
            _settingsState.value = _settingsState.value.copy(spotifyConnected = false)
        }
    }

    /** Re-reads connection state, e.g. after returning from the consent page. */
    fun refreshSpotifyState() {
        _settingsState.value =
            _settingsState.value.copy(spotifyConnected = spotifyAuthManager.isConnected)
    }
}
