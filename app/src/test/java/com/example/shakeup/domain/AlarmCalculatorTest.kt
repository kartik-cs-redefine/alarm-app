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
        // Now = 2026-01-01 07:00:00
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
        // Now = 2026-01-01 07:00:00
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expected = LocalDateTime.of(2026, 1, 2, 6, 0).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }

    @Test
    fun repeatingAlarm_todayInFuture_triggersToday() {
        // 2026-01-01 is THURSDAY
        val alarm = Alarm(
            hour = 9,
            minute = 0,
            daysOfWeek = setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            isEnabled = true
        )
        // Now = Thursday 2026-01-01 07:00:00
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expected = LocalDateTime.of(2026, 1, 1, 9, 0).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }

    @Test
    fun repeatingAlarm_todayInPast_triggersNextMatchingDay() {
        // 2026-01-01 is THURSDAY
        val alarm = Alarm(
            hour = 6,
            minute = 0,
            daysOfWeek = setOf(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
            isEnabled = true
        )
        // Now = Thursday 2026-01-01 07:00:00
        val now = LocalDateTime.of(2026, 1, 1, 7, 0).atZone(zoneId).toInstant().toEpochMilli()
        // Next matching day is SATURDAY 2026-01-03 06:00:00
        val expected = LocalDateTime.of(2026, 1, 3, 6, 0).atZone(zoneId).toInstant().toEpochMilli()

        val actual = AlarmCalculator.calculateNextTriggerTimeMillis(alarm, now, zoneId)
        assertEquals(expected, actual)
    }
}
