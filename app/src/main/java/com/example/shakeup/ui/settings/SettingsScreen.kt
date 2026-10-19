package com.example.shakeup.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.shakeup.R
import com.example.shakeup.domain.ShakeSensitivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSnoozeChange: (Int) -> Unit,
    onSensitivityChange: (ShakeSensitivity) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cancel)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Default Snooze Minutes
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.default_snooze),
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    listOf(5, 10, 15, 20).forEachIndexed { index, minutes ->
                        SegmentedButton(
                            selected = uiState.defaultSnoozeMinutes == minutes,
                            onClick = { onSnoozeChange(minutes) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 4)
                        ) {
                            Text(text = "$minutes min")
                        }
                    }
                }
            }

            // Default Shake Sensitivity
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.default_sensitivity),
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    ShakeSensitivity.entries.forEachIndexed { index, sensitivity ->
                        val labelRes = when (sensitivity) {
                            ShakeSensitivity.EASY -> R.string.shake_sensitivity_low
                            ShakeSensitivity.MEDIUM -> R.string.shake_sensitivity_medium
                            ShakeSensitivity.HARD -> R.string.shake_sensitivity_high
                        }
                        SegmentedButton(
                            selected = uiState.defaultShakeSensitivity == sensitivity,
                            onClick = { onSensitivityChange(sensitivity) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) {
                            Text(text = stringResource(id = labelRes))
                        }
                    }
                }
            }

            // Default Vibrate Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.vibrate_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = uiState.defaultVibrate,
                    onCheckedChange = onVibrateChange
                )
            }
        }
    }
}
