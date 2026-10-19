package com.example.shakeup.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.shakeup.ui.detail.AlarmDetailScreen
import com.example.shakeup.ui.detail.AlarmDetailViewModel
import com.example.shakeup.ui.list.AlarmListScreen
import com.example.shakeup.ui.list.AlarmListViewModel
import com.example.shakeup.ui.navigation.Screen
import com.example.shakeup.ui.settings.SettingsScreen
import com.example.shakeup.ui.settings.SettingsViewModel
import com.example.shakeup.ui.theme.ShakeUpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        setContent {
            ShakeUpTheme {
                MainNavigation(
                    onRequestPermission = { openExactAlarmSettings() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MainNavigation(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.AlarmList,
        modifier = modifier
    ) {
        composable<Screen.AlarmList> {
            val viewModel: AlarmListViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AlarmListScreen(
                uiState = uiState,
                onAddAlarm = { navController.navigate(Screen.AlarmDetail(0L)) },
                onEditAlarm = { alarm -> navController.navigate(Screen.AlarmDetail(alarm.id)) },
                onToggleAlarm = { alarm, isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                onOpenSettings = { navController.navigate(Screen.Settings) },
                onRequestPermission = onRequestPermission
            )
        }

        composable<Screen.AlarmDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AlarmDetail>()
            val viewModel: AlarmDetailViewModel = hiltViewModel()
            LaunchedEffect(route.alarmId) {
                viewModel.loadAlarm(route.alarmId)
            }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AlarmDetailScreen(
                uiState = uiState,
                onTimeChange = { hour, minute -> viewModel.updateTime(hour, minute) },
                onLabelChange = { label -> viewModel.updateLabel(label) },
                onToggleDay = { day -> viewModel.toggleDay(day) },
                onShakeSensitivityChange = { sens -> viewModel.updateShakeSensitivity(sens) },
                onChallengeTypeChange = { type -> viewModel.updateChallengeType(type) },
                onToggleVibrate = { vib -> viewModel.toggleVibrate(vib) },
                onSave = { viewModel.saveAlarm() },
                onDelete = { viewModel.deleteAlarm() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            SettingsScreen(
                uiState = uiState,
                onSnoozeChange = { minutes -> viewModel.updateSnoozeMinutes(minutes) },
                onSensitivityChange = { sens -> viewModel.updateShakeSensitivity(sens) },
                onVibrateChange = { vib -> viewModel.updateVibrate(vib) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
