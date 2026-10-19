package com.example.shakeup.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.shakeup.data.dao.AlarmDao
import com.example.shakeup.data.database.AppDatabase
import com.example.shakeup.data.entity.AlarmEntity
import com.example.shakeup.domain.ShakeSensitivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.DayOfWeek

@RunWith(RobolectricTestRunner::class)
class AlarmDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AlarmDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.alarmDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAlarm() = runBlocking {
        val alarm = AlarmEntity(
            hour = 7,
            minute = 30,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            isEnabled = true,
            label = "Morning Workout",
            shakeSensitivity = ShakeSensitivity.HARD,
            vibrate = true,
            soundUri = "content://settings/system/alarm_alert",
            snoozeMinutes = 10
        )

        val id = dao.insertAlarm(alarm)
        val retrieved = dao.getAlarmById(id)

        assertNotNull(retrieved)
        assertEquals(7, retrieved?.hour)
        assertEquals(30, retrieved?.minute)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), retrieved?.daysOfWeek)
        assertEquals("Morning Workout", retrieved?.label)
        assertEquals(ShakeSensitivity.HARD, retrieved?.shakeSensitivity)
    }

    @Test
    fun deleteAlarm() = runBlocking {
        val alarm = AlarmEntity(
            hour = 9,
            minute = 0,
            daysOfWeek = emptySet(),
            isEnabled = true,
            label = "Test",
            shakeSensitivity = ShakeSensitivity.EASY,
            vibrate = false,
            soundUri = null,
            snoozeMinutes = 5
        )

        val id = dao.insertAlarm(alarm)
        dao.deleteAlarmById(id)

        val retrieved = dao.getAlarmById(id)
        assertNull(retrieved)
    }

    @Test
    fun getAllAlarmsFlow() = runBlocking {
        val alarm1 = AlarmEntity(
            hour = 10, minute = 0, daysOfWeek = emptySet(),
            isEnabled = true, label = "Later", shakeSensitivity = ShakeSensitivity.MEDIUM,
            vibrate = true, soundUri = null, snoozeMinutes = 5
        )
        val alarm2 = AlarmEntity(
            hour = 6, minute = 30, daysOfWeek = emptySet(),
            isEnabled = true, label = "Earlier", shakeSensitivity = ShakeSensitivity.MEDIUM,
            vibrate = true, soundUri = null, snoozeMinutes = 5
        )

        dao.insertAlarm(alarm1)
        dao.insertAlarm(alarm2)

        val list = dao.getAllAlarms().first()
        assertEquals(2, list.size)
        // Ordered by hour ASC
        assertEquals("Earlier", list[0].label)
        assertEquals("Later", list[1].label)
    }
}
