package com.example.shakeup.ui.settings

import com.example.shakeup.domain.ShakeSensitivity

data class SettingsUiState(
    val defaultSnoozeMinutes: Int = 5,
    val defaultShakeSensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
    val defaultRingtoneUri: String? = null,
    val defaultVibrate: Boolean = true
)
