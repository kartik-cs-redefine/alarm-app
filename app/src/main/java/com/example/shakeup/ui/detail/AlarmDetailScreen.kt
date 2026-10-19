package com.example.shakeup.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.shakeup.R
import com.example.shakeup.domain.ShakeSensitivity
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    uiState: AlarmDetailUiState,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    onLabelChange: (String) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onShakeSensitivityChange: (ShakeSensitivity) -> Unit,
    onToggleVibrate: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = uiState.hour,
        initialMinute = uiState.minute,
        is24Hour = true
    )

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(timePickerState.hour, timePickerState.minute)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.alarmId > 0) {
                            stringResource(id = R.string.edit_alarm)
                        } else {
                            stringResource(id = R.string.add_alarm)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cancel)
                        )
                    }
                },
                actions = {
                    if (uiState.alarmId > 0) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_alarm)
                            )
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimePicker(state = timePickerState)

            OutlinedTextField(
                value = uiState.label,
                onValueChange = onLabelChange,
                label = { Text(text = stringResource(id = R.string.alarm_label_default)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Days of week selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.alarm_days),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val isSelected = uiState.daysOfWeek.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleDay(day) },
                            label = { Text(text = day.name.take(1)) }
                        )
                    }
                }
            }

            // Shake Sensitivity Segmented Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.shake_sensitivity),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ShakeSensitivity.entries.forEachIndexed { index, sensitivity ->
                        val labelRes = when (sensitivity) {
                            ShakeSensitivity.EASY -> R.string.shake_sensitivity_low
                            ShakeSensitivity.MEDIUM -> R.string.shake_sensitivity_medium
                            ShakeSensitivity.HARD -> R.string.shake_sensitivity_high
                        }
                        SegmentedButton(
                            selected = uiState.shakeSensitivity == sensitivity,
                            onClick = { onShakeSensitivityChange(sensitivity) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ShakeSensitivity.entries.size
                            )
                        ) {
                            Text(text = stringResource(id = labelRes))
                        }
                    }
                }
            }

            // Vibrate Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vibrate",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = uiState.vibrate,
                    onCheckedChange = onToggleVibrate
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}
