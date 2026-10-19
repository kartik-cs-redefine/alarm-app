package com.example.shakeup.data.converter

import androidx.room.TypeConverter
import com.example.shakeup.domain.DismissChallengeType
import com.example.shakeup.domain.ShakeSensitivity
import java.time.DayOfWeek

class Converters {

    @TypeConverter
    fun fromDaysOfWeek(days: Set<DayOfWeek>?): String {
        return days?.joinToString(",") { it.name } ?: ""
    }

    @TypeConverter
    fun toDaysOfWeek(data: String?): Set<DayOfWeek> {
        if (data.isNullOrEmpty()) return emptySet()
        return data.split(",")
            .filter { it.isNotBlank() }
            .map { DayOfWeek.valueOf(it) }
            .toSet()
    }

    @TypeConverter
    fun fromShakeSensitivity(sensitivity: ShakeSensitivity?): String {
        return sensitivity?.name ?: ShakeSensitivity.MEDIUM.name
    }

    @TypeConverter
    fun toShakeSensitivity(data: String?): ShakeSensitivity {
        if (data.isNullOrBlank()) return ShakeSensitivity.MEDIUM
        return try {
            ShakeSensitivity.valueOf(data)
        } catch (e: Exception) {
            ShakeSensitivity.MEDIUM
        }
    }

    @TypeConverter
    fun fromDismissChallengeType(type: DismissChallengeType?): String {
        return type?.name ?: DismissChallengeType.SHAKE.name
    }

    @TypeConverter
    fun toDismissChallengeType(data: String?): DismissChallengeType {
        if (data.isNullOrBlank()) return DismissChallengeType.SHAKE
        return try {
            DismissChallengeType.valueOf(data)
        } catch (e: Exception) {
            DismissChallengeType.SHAKE
        }
    }
}
