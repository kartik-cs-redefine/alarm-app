package com.example.shakeup.ui

import com.example.shakeup.alarm.AlarmScheduler
import com.example.shakeup.data.repository.AlarmRepository
import com.example.shakeup.domain.Alarm
import com.example.shakeup.ui.list.AlarmListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: AlarmRepository = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { alarmScheduler.canScheduleExactAlarms() } returns true
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAlarms_updatesStateWithAlarms() = runTest {
        val alarms = listOf(
            Alarm(id = 1, hour = 8, minute = 0, label = "Test Alarm")
        )
        every { repository.getAllAlarms() } returns flowOf(alarms)

        val viewModel = AlarmListViewModel(repository, alarmScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.alarms.size)
        assertEquals("Test Alarm", state.alarms[0].label)
    }

    @Test
    fun toggleAlarm_schedulesWhenEnabled() = runTest {
        every { repository.getAllAlarms() } returns flowOf(emptyList())
        coEvery { repository.saveAlarm(any()) } returns 1L

        val viewModel = AlarmListViewModel(repository, alarmScheduler)
        val alarm = Alarm(id = 1, hour = 7, minute = 30, isEnabled = false)

        viewModel.toggleAlarm(alarm, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.saveAlarm(match { it.isEnabled }) }
        coVerify { alarmScheduler.schedule(match { it.isEnabled }) }
    }
}
