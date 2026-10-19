package com.example.shakeup.ui

import com.example.shakeup.alarm.AlarmScheduler
import com.example.shakeup.data.repository.AlarmRepository
import com.example.shakeup.domain.Alarm
import com.example.shakeup.domain.ShakeSensitivity
import com.example.shakeup.ui.detail.AlarmDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: AlarmRepository = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAlarm_populatesState() = runTest {
        val alarm = Alarm(
            id = 5,
            hour = 6,
            minute = 45,
            daysOfWeek = setOf(DayOfWeek.MONDAY),
            label = "Morning",
            shakeSensitivity = ShakeSensitivity.HARD
        )
        coEvery { repository.getAlarmById(5) } returns alarm

        val viewModel = AlarmDetailViewModel(repository, alarmScheduler)
        viewModel.loadAlarm(5)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5L, state.alarmId)
        assertEquals(6, state.hour)
        assertEquals(45, state.minute)
        assertEquals("Morning", state.label)
        assertEquals(ShakeSensitivity.HARD, state.shakeSensitivity)
    }

    @Test
    fun saveAlarm_savesToRepoAndSchedules() = runTest {
        coEvery { repository.saveAlarm(any()) } returns 10L

        val viewModel = AlarmDetailViewModel(repository, alarmScheduler)
        viewModel.updateTime(7, 15)
        viewModel.updateLabel("Workout")
        viewModel.saveAlarm()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.saveAlarm(match { it.hour == 7 && it.minute == 15 && it.label == "Workout" }) }
        coVerify { alarmScheduler.schedule(match { it.id == 10L }) }
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
