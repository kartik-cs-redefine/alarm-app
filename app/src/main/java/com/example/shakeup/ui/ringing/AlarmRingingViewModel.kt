package com.example.shakeup.ui.ringing

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shakeup.alarm.AlarmService
import com.example.shakeup.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: Long = savedStateHandle.get<Long>(AlarmService.EXTRA_ALARM_ID) ?: -1L

    private val _uiState = MutableStateFlow(AlarmRingingUiState())
    val uiState: StateFlow<AlarmRingingUiState> = _uiState.asStateFlow()

    init {
        loadAlarmDetails()
        observeActiveAlarm()
    }

    private fun loadAlarmDetails() {
        if (alarmId != -1L) {
            viewModelScope.launch {
                val alarm = repository.getAlarmById(alarmId)
                if (alarm != null) {
                    _uiState.update { it.copy(alarm = alarm, requiredShakes = alarm.shakeSensitivity.requiredShakes) }
                }
            }
        }
    }

    private fun observeActiveAlarm() {
        viewModelScope.launch {
            AlarmService.activeAlarmState.collect { activeData ->
                if (activeData != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            alarm = activeData.alarm,
                            currentShakes = activeData.currentShakes,
                            requiredShakes = activeData.requiredShakes
                        )
                    }
                }
            }
        }
    }

    fun snooze() {
        val intent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE_ALARM
        }
        context.startService(intent)
    }

    fun dismiss() {
        val intent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_DISMISS_ALARM
        }
        context.startService(intent)
        _uiState.update { it.copy(isDismissed = true) }
    }
}
