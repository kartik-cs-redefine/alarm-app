package com.example.shakeup.ui.detail

import com.example.shakeup.domain.ShakeSensitivity
import java.time.DayOfWeek

data class AlarmDetailUiState(
    val alarmId: Long = 0L,
    val hour: Int = 8,
    val minute: Int = 0,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val isEnabled: Boolean = true,
    val label: String = "",
    val shakeSensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
    val snoozeMinutes: Int = 5,
    val isSaved: Boolean = false
)
