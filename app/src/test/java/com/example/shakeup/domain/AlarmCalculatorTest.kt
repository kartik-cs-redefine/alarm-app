package com.example.shakeup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmCalculatorTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun disabledAlarm_returnsNull() {
        val alarm = Alarm(
            hour = 8,
            minute = 0,
            isEnabled = false
        )
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val result = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertNull(result)
    }

    @Test
    fun oneTimeAlarm_futureToday_triggersToday() {
        val alarm = Alarm(
            hour = 8,
            minute = 30,
            daysOfWeek = emptySet(),
            isEnabled = true
        )
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expected = LocalDateTime.of(2026, 1, 1, 8, 30).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }

    @Test
    fun oneTimeAlarm_pastToday_triggersTomorrow() {
        val alarm = Alarm(
            hour = 6,
            minute = 0,
            daysOfWeek = emptySet(),
            isEnabled = true
        )
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expected = LocalDateTime.of(2026, 1, 2, 6, 0).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }

    @Test
    fun repeatingAlarm_todayInFuture_triggersToday() {
        val alarm = Alarm(
            hour = 9,
            minute = 0,
            daysOfWeek = setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            isEnabled = true
        )
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expected = LocalDateTime.of(2026, 1, 1, 9, 0).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }

    @Test
    fun repeatingAlarm_todayInPast_triggersNextMatchingDay() {
        val alarm = Alarm(
            hour = 6,
            minute = 0,
            daysOfWeek = setOf(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
            isEnabled = true
        )
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expected = LocalDateTime.of(2026, 1, 3, 6, 0).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }

    @Test
    fun snoozeReschedulesExactlyOnce() {
        val alarm = Alarm(id = 1, hour = 8, minute = 0, snoozeMinutes = 10)
        val now = LocalDateTime.of(2026, 1, 1, 8, 0).atZone(zoneId).toInstant().toEpochMilli()
        val snoozeMillis = now + (alarm.snoozeMinutes * 60 * 1000L)
        val expected = LocalDateTime.of(2026, 1, 1, 8, 10).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expected, snoozeMillis)
    }

    @Test
    fun repeatingAlarmReschedulesForNextMatchingDayAfterDismiss() {
        // Monday 2026-03-16 08:05:00 (after 08:00 alarm dismissed)
        val alarm = Alarm(
            id = 2,
            hour = 8,
            minute = 0,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            isEnabled = true
        )
        val now = LocalDateTime.of(2026, 3, 16, 8, 5).atZone(zoneId).toInstant().toEpochMilli()
        val nextTrigger = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        val expectedWednesday = LocalDateTime.of(2026, 3, 18, 8, 0).atZone(zoneId).toInstant().toEpochMilli()
        assertEquals(expectedWednesday, nextTrigger)
    }

    @Test
    fun oneTimeAlarmDisablesItselfAfterDismiss() {
        val alarm = Alarm(
            id = 3,
            hour = 8,
            minute = 0,
            daysOfWeek = emptySet(),
            isEnabled = true
        )
        val dismissedAlarm = alarm.copy(isEnabled = false)
        val now = LocalDateTime.of(2026, 3, 16, 8, 5).atZone(zoneId).toInstant().toEpochMilli()
        val nextTrigger = AlarmCalculator.calculateNextTriggerTimeMillis(dismissedAlarm, now, zoneId)
        assertNull(nextTrigger)
    }
}
