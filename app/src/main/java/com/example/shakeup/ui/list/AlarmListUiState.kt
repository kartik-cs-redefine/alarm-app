package com.example.shakeup.ui.list

import com.example.shakeup.domain.Alarm

data class AlarmListUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = true,
    val showPermissionBanner: Boolean = false
)
