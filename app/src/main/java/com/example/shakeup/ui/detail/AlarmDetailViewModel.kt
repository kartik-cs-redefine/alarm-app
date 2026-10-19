package com.example.shakeup.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shakeup.alarm.AlarmScheduler
import com.example.shakeup.data.repository.AlarmRepository
import com.example.shakeup.domain.Alarm
import com.example.shakeup.domain.DismissChallengeType
import com.example.shakeup.domain.ShakeSensitivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmDetailUiState())
    val uiState: StateFlow<AlarmDetailUiState> = _uiState.asStateFlow()

    fun loadAlarm(alarmId: Long) {
        if (alarmId <= 0) return
        viewModelScope.launch {
            val alarm = repository.getAlarmById(alarmId)
            if (alarm != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        alarmId = alarm.id,
                        hour = alarm.hour,
                        minute = alarm.minute,
                        daysOfWeek = alarm.daysOfWeek,
                        isEnabled = alarm.isEnabled,
                        label = alarm.label,
                        shakeSensitivity = alarm.shakeSensitivity,
                        vibrate = alarm.vibrate,
                        soundUri = alarm.soundUri,
                        snoozeMinutes = alarm.snoozeMinutes,
                        challengeType = alarm.challengeType
                    )
                }
            }
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun toggleDay(day: DayOfWeek) {
        _uiState.update { currentState ->
            val updatedDays = if (currentState.daysOfWeek.contains(day)) {
                currentState.daysOfWeek - day
            } else {
                currentState.daysOfWeek + day
            }
            currentState.copy(daysOfWeek = updatedDays)
        }
    }

    fun updateShakeSensitivity(sensitivity: ShakeSensitivity) {
        _uiState.update { it.copy(shakeSensitivity = sensitivity) }
    }

    fun updateChallengeType(type: DismissChallengeType) {
        _uiState.update { it.copy(challengeType = type) }
    }

    fun toggleVibrate(vibrate: Boolean) {
        _uiState.update { it.copy(vibrate = vibrate) }
    }

    fun saveAlarm() {
        viewModelScope.launch {
            val state = uiState.value
            val alarm = Alarm(
                id = state.alarmId,
                hour = state.hour,
                minute = state.minute,
                daysOfWeek = state.daysOfWeek,
                isEnabled = state.isEnabled,
                label = state.label,
                shakeSensitivity = state.shakeSensitivity,
                vibrate = state.vibrate,
                soundUri = state.soundUri,
                snoozeMinutes = state.snoozeMinutes,
                challengeType = state.challengeType
            )
            val savedId = repository.saveAlarm(alarm)
            val alarmToSchedule = alarm.copy(id = savedId)
            alarmScheduler.schedule(alarmToSchedule)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteAlarm() {
        viewModelScope.launch {
            val alarmId = uiState.value.alarmId
            if (alarmId > 0) {
                alarmScheduler.cancel(alarmId)
                repository.deleteAlarmById(alarmId)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
