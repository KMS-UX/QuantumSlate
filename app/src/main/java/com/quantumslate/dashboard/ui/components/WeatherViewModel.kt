package com.quantumslate.dashboard.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.repository.WeatherLocationResolver
import com.quantumslate.dashboard.data.repository.WeatherRepository
import com.quantumslate.dashboard.domain.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val weather: Weather? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val weatherLocationResolver: WeatherLocationResolver,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _weatherState = MutableStateFlow(WeatherUiState())
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    init {
        observeWeather()
    }

    private fun observeWeather() {
        viewModelScope.launch {
            weatherRepository.weather.collect { weather ->
                _weatherState.value = WeatherUiState(
                    weather = weather,
                    isLoading = false
                )
            }
        }
    }

    fun refreshWeather(location: String? = null) {
        viewModelScope.launch {
            _weatherState.value = _weatherState.value.copy(isLoading = true)

            val result = if (location != null) {
                weatherRepository.fetchWeatherByLocationName(location)
            } else {
                // Use default location or last known location
                // For now, use a default location
                weatherLocationResolver.fetchForCurrentLocation()
            }

            result.onFailure { error ->
                _weatherState.value = _weatherState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to fetch weather"
                )
            }
        }
    }

    fun loadCachedWeather() {
        viewModelScope.launch {
            val cached = weatherRepository.getCachedWeather()
            _weatherState.value = WeatherUiState(
                weather = cached,
                isLoading = false
            )
        }
    }
}
