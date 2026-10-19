package com.example.shakeup.domain

import java.time.DayOfWeek

data class Alarm(
    val id: Long = 0,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val daysOfWeek: Set<DayOfWeek> = emptySet(), // Empty means non-repeating (one-time)
    val isEnabled: Boolean = true,
    val label: String = "",
    val shakeSensitivity: ShakeSensitivity = ShakeSensitivity.MEDIUM,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
    val snoozeMinutes: Int = 5,
    val challengeType: DismissChallengeType = DismissChallengeType.SHAKE
) {
    val isRepeating: Boolean
        get() = daysOfWeek.isNotEmpty()
}
