package com.example.shakeup.ui.ringing

import android.content.Context
import android.content.Intent
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
    private val repository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmRingingUiState())
    val uiState: StateFlow<AlarmRingingUiState> = _uiState.asStateFlow()

    init {
        observeActiveAlarm()
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
                } else {
                    _uiState.update { it.copy(isDismissed = true) }
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
    }
}
