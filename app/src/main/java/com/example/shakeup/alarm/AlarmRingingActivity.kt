package com.example.shakeup.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shakeup.ui.ringing.AlarmRingingScreen
import com.example.shakeup.ui.ringing.AlarmRingingViewModel
import com.example.shakeup.ui.theme.ShakeUpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    private val viewModel: AlarmRingingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show on lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        setContent {
            ShakeUpTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                AlarmRingingScreen(
                    uiState = uiState,
                    onSnooze = { viewModel.snooze() },
                    onDismiss = { viewModel.dismiss() },
                    onFinishActivity = { finish() }
                )
            }
        }
    }
}
