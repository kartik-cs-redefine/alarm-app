package com.example.shakeup.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.shakeup.domain.Alarm
import com.example.shakeup.domain.ShakeSensitivity
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Set<DayOfWeek>,
    val isEnabled: Boolean,
    val label: String,
    val shakeSensitivity: ShakeSensitivity,
    val vibrate: Boolean,
    val soundUri: String?,
    val snoozeMinutes: Int
) {
    fun toDomain(): Alarm {
        return Alarm(
            id = id,
            hour = hour,
            minute = minute,
            daysOfWeek = daysOfWeek,
            isEnabled = isEnabled,
            label = label,
            shakeSensitivity = shakeSensitivity,
            vibrate = vibrate,
            soundUri = soundUri,
            snoozeMinutes = snoozeMinutes
        )
    }

    companion object {
        fun fromDomain(alarm: Alarm): AlarmEntity {
            return AlarmEntity(
                id = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                daysOfWeek = alarm.daysOfWeek,
                isEnabled = alarm.isEnabled,
                label = alarm.label,
                shakeSensitivity = alarm.shakeSensitivity,
                vibrate = alarm.vibrate,
                soundUri = alarm.soundUri,
                snoozeMinutes = alarm.snoozeMinutes
            )
        }
    }
}
