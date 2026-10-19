package com.example.shakeup.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {

    @Serializable
    data object AlarmList : Screen

    @Serializable
    data class AlarmDetail(val alarmId: Long = 0L) : Screen

    @Serializable
    data object Settings : Screen
}
