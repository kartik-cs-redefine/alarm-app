package com.example.shakeup.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shakeup.data.repository.SettingsRepository
import com.example.shakeup.domain.ShakeSensitivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.defaultSnoozeMinutes,
                settingsRepository.defaultShakeSensitivity,
                settingsRepository.defaultRingtoneUri,
                settingsRepository.defaultVibrate
            ) { snooze, sensitivity, ringtone, vibrate ->
                SettingsUiState(
                    defaultSnoozeMinutes = snooze,
                    defaultShakeSensitivity = sensitivity,
                    defaultRingtoneUri = ringtone,
                    defaultVibrate = vibrate
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateSnoozeMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultSnoozeMinutes(minutes)
        }
    }

    fun updateShakeSensitivity(sensitivity: ShakeSensitivity) {
        viewModelScope.launch {
            settingsRepository.setDefaultShakeSensitivity(sensitivity)
        }
    }

    fun updateVibrate(vibrate: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDefaultVibrate(vibrate)
        }
    }
}
