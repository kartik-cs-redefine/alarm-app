package com.example.shakeup.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object AlarmCalculator {

    /**
     * Calculates the next epoch millisecond timestamp at which the alarm should trigger.
     *
     * @param alarm The alarm model
     * @param nowMillis The current timestamp in milliseconds
     * @param zoneId The system or target time zone
     * @return Epoch milliseconds for the next trigger time, or null if alarm is disabled.
     */
    fun calculateNextTriggerTimeMillis(
        alarm: Alarm,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (!alarm.isEnabled) return null

        val nowLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zoneId)
        val todayAlarmTime = nowLocalDateTime.withHour(alarm.hour)
            .withMinute(alarm.minute)
            .withSecond(0)
            .withNano(0)

        if (!alarm.isRepeating) {
            // One-time alarm
            val triggerDateTime = if (todayAlarmTime.isAfter(nowLocalDateTime)) {
                todayAlarmTime
            } else {
                todayAlarmTime.plusDays(1)
            }
            return triggerDateTime.atZone(zoneId).toInstant().toEpochMilli()
        } else {
            // Repeating alarm across daysOfWeek
            var candidate = todayAlarmTime
            // Check today first if the time hasn't passed and today is in daysOfWeek
            if (candidate.isAfter(nowLocalDateTime) && alarm.daysOfWeek.contains(candidate.dayOfWeek)) {
                return candidate.atZone(zoneId).toInstant().toEpochMilli()
            }

            // Look up to 7 days into the future
            for (dayOffset in 1..7) {
                val nextCandidate = todayAlarmTime.plusDays(dayOffset.toLong())
                if (alarm.daysOfWeek.contains(nextCandidate.dayOfWeek)) {
                    return nextCandidate.atZone(zoneId).toInstant().toEpochMilli()
                }
            }

            // Fallback (should not be reached if daysOfWeek is non-empty)
            return todayAlarmTime.plusDays(1).atZone(zoneId).toInstant().toEpochMilli()
        }
    }
}
