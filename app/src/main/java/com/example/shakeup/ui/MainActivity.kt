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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.shakeup.R
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
    val configuration = LocalConfiguration.current
    val isTwoPane = configuration.screenWidthDp.dp >= 600.dp

    if (isTwoPane) {
        TwoPaneAdaptiveLayout(
            onRequestPermission = onRequestPermission,
            modifier = modifier
        )
    } else {
        SinglePaneNavigation(
            onRequestPermission = onRequestPermission,
            modifier = modifier
        )
    }
}

@Composable
private fun TwoPaneAdaptiveLayout(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAlarmId by remember { mutableStateOf<Long?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    val listViewModel: AlarmListViewModel = hiltViewModel()
    val listUiState by listViewModel.uiState.collectAsStateWithLifecycle()

    Row(modifier = modifier) {
        // Left Pane: Alarm List
        Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
            AlarmListScreen(
                uiState = listUiState,
                onAddAlarm = {
                    showSettings = false
                    selectedAlarmId = 0L
                },
                onEditAlarm = { alarm ->
                    showSettings = false
                    selectedAlarmId = alarm.id
                },
                onToggleAlarm = { alarm, isEnabled -> listViewModel.toggleAlarm(alarm, isEnabled) },
                onDeleteAlarm = { alarm -> listViewModel.deleteAlarm(alarm) },
                onOpenSettings = { showSettings = true },
                onRequestPermission = onRequestPermission
            )
        }

        // Divider / Right Pane: Detail, Settings, or Placeholder
        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showSettings) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = settingsUiState,
                    onSnoozeChange = { min -> settingsViewModel.updateSnoozeMinutes(min) },
                    onSensitivityChange = { sens -> settingsViewModel.updateShakeSensitivity(sens) },
                    onVibrateChange = { vib -> settingsViewModel.updateVibrate(vib) },
                    onNavigateBack = { showSettings = false }
                )
            } else if (selectedAlarmId != null) {
                val detailViewModel: AlarmDetailViewModel = hiltViewModel()
                val alarmId = selectedAlarmId ?: 0L

                LaunchedEffect(alarmId) {
                    detailViewModel.loadAlarm(alarmId)
                }

                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

                AlarmDetailScreen(
                    uiState = detailUiState,
                    onTimeChange = { hour, min -> detailViewModel.updateTime(hour, min) },
                    onLabelChange = { label -> detailViewModel.updateLabel(label) },
                    onToggleDay = { day -> detailViewModel.toggleDay(day) },
                    onShakeSensitivityChange = { sens -> detailViewModel.updateShakeSensitivity(sens) },
                    onChallengeTypeChange = { type -> detailViewModel.updateChallengeType(type) },
                    onToggleVibrate = { vib -> detailViewModel.toggleVibrate(vib) },
                    onSave = {
                        detailViewModel.saveAlarm()
                        selectedAlarmId = null
                    },
                    onDelete = {
                        detailViewModel.deleteAlarm()
                        selectedAlarmId = null
                    },
                    onNavigateBack = { selectedAlarmId = null }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_alarms_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SinglePaneNavigation(
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
