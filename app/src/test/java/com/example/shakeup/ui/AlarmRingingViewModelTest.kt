package com.example.shakeup.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.shakeup.alarm.AlarmService
import com.example.shakeup.data.repository.AlarmRepository
import com.example.shakeup.domain.Alarm
import com.example.shakeup.ui.ringing.AlarmRingingViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AlarmRingingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val repository: AlarmRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialNullActiveAlarmState_doesNotCloseScreen() = runTest {
        val alarm = Alarm(id = 10, hour = 7, minute = 0, label = "Ringing Test")
        coEvery { repository.getAlarmById(10) } returns alarm

        val savedStateHandle = SavedStateHandle(mapOf(AlarmService.EXTRA_ALARM_ID to 10L))
        val viewModel = AlarmRingingViewModel(context, repository, savedStateHandle)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDismissed)
        assertEquals(10L, state.alarm?.id)
        assertEquals("Ringing Test", state.alarm?.label)
    }
}
