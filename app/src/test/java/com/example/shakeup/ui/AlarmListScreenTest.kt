package com.example.shakeup.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.shakeup.TestActivity
import com.example.shakeup.ui.list.AlarmListScreen
import com.example.shakeup.ui.list.AlarmListUiState
import com.example.shakeup.ui.theme.ShakeUpTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlarmListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun emptyListScreen_showsAddButtonAndEmptyText() {
        val state = AlarmListUiState(alarms = emptyList(), isLoading = false)

        composeTestRule.setContent {
            ShakeUpTheme {
                AlarmListScreen(
                    uiState = state,
                    onAddAlarm = {},
                    onEditAlarm = {},
                    onToggleAlarm = { _, _ -> },
                    onDeleteAlarm = {},
                    onOpenSettings = {},
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No alarms set. Tap + to add an alarm.").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Alarm").assertIsDisplayed()
    }
}
