package com.example.shakeup.ui.ringing

import com.example.shakeup.domain.Alarm

data class AlarmRingingUiState(
    val alarm: Alarm? = null,
    val currentShakes: Int = 0,
    val requiredShakes: Int = 20,
    val isDismissed: Boolean = false
) {
    val progress: Float
        get() = if (requiredShakes > 0) (currentShakes.toFloat() / requiredShakes.toFloat()).coerceIn(0f, 1f) else 0f
}
