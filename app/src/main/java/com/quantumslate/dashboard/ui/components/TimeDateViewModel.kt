package com.quantumslate.dashboard.ui.components

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quantumslate.dashboard.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeDateViewModel @Inject constructor(
    application: Application,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _is24HourFormat = MutableStateFlow(true)
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    init {
        // Start time ticker
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _currentTime.value = System.currentTimeMillis()
            }
        }

        // Load user preference for time format
        loadTimeFormatPreference()
    }

    private fun loadTimeFormatPreference() {
        // For now, default to 24-hour format
        // Can be extended to read from preferences
        _is24HourFormat.value = true
    }

    fun setTimeFormat(is24Hour: Boolean) {
        _is24HourFormat.value = is24Hour
    }
}
