package com.example.shakeup.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shakeup.alarm.AlarmScheduler
import com.example.shakeup.data.repository.AlarmRepository
import com.example.shakeup.domain.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmListUiState())
    val uiState: StateFlow<AlarmListUiState> = _uiState.asStateFlow()

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            repository.getAllAlarms().collect { alarms ->
                _uiState.update { currentState ->
                    currentState.copy(
                        alarms = alarms,
                        isLoading = false,
                        showPermissionBanner = !alarmScheduler.canScheduleExactAlarms()
                    )
                }
            }
        }
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = isEnabled)
            repository.saveAlarm(updated)
            if (isEnabled) {
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(updated.id)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmScheduler.cancel(alarm.id)
            repository.deleteAlarm(alarm)
        }
    }

    fun refreshPermissionState() {
        _uiState.update { currentState ->
            currentState.copy(showPermissionBanner = !alarmScheduler.canScheduleExactAlarms())
        }
    }
}
