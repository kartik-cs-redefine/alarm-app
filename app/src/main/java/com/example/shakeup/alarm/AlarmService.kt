package com.example.shakeup.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.shakeup.R
import com.example.shakeup.data.repository.AlarmRepository
import com.example.shakeup.domain.Alarm
import com.example.shakeup.domain.ShakeSensitivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var shakeDetector: ShakeDetector? = null

    private var currentAlarm: Alarm? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground immediately with a fallback notification to prevent ANR
        startForeground(NOTIFICATION_ID, createFallbackNotification())

        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)

        when (action) {
            ACTION_START_ALARM -> {
                serviceScope.launch {
                    val alarm = if (alarmId != -1L) repository.getAlarmById(alarmId) else null
                    if (alarm != null) {
                        startAlarm(alarm)
                    } else {
                        // Fallback generic alarm if DB lookup fails
                        val fallback = Alarm(id = alarmId, hour = 0, minute = 0, label = "Alarm")
                        startAlarm(fallback)
                    }
                }
            }
            ACTION_DISMISS_ALARM -> {
                dismissAlarm()
            }
            ACTION_SNOOZE_ALARM -> {
                snoozeAlarm()
            }
            else -> stopSelf()
        }

        return START_STICKY
    }

    private fun startAlarm(alarm: Alarm) {
        currentAlarm = alarm
        _activeAlarmState.value = ActiveAlarmData(
            alarm = alarm,
            currentShakes = 0,
            requiredShakes = alarm.shakeSensitivity.requiredShakes
        )

        val notification = createRingingNotification(alarm)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        startRingtone(alarm.soundUri)
        if (alarm.vibrate) {
            startVibration()
        }
        startShakeDetection(alarm.shakeSensitivity)
    }

    private fun startRingtone(soundUriString: String?) {
        try {
            val uri = if (!soundUriString.isNull_or_empty()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setContextAndSource(this@AlarmService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun MediaPlayer.setContextAndSource(context: Context, uri: Uri) {
        setDataSource(context, uri)
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun startShakeDetection(sensitivity: ShakeSensitivity) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(
            sensitivity = sensitivity,
            onShakeProgress = { current, required ->
                currentAlarm?.let { alarm ->
                    _activeAlarmState.value = ActiveAlarmData(alarm, current, required)
                }
            },
            onDismiss = {
                dismissAlarm()
            }
        )
        shakeDetector?.start(sensorManager)
    }

    private fun dismissAlarm() {
        stopAudioAndVibration()
        serviceScope.launch {
            currentAlarm?.let { alarm ->
                if (alarm.id > 0) {
                    if (alarm.isRepeating) {
                        alarmScheduler.schedule(alarm)
                    } else {
                        repository.saveAlarm(alarm.copy(isEnabled = false))
                    }
                }
            }
            _activeAlarmState.value = null
            stopSelf()
        }
    }

    private fun snoozeAlarm() {
        stopAudioAndVibration()
        serviceScope.launch {
            currentAlarm?.let { alarm ->
                if (alarm.id > 0) {
                    val snoozeMillis = System.currentTimeMillis() + (alarm.snoozeMinutes * 60 * 1000)
                    val intent = Intent(this@AlarmService, AlarmReceiver::class.java).apply {
                        action = AlarmScheduler.ACTION_TRIGGER_ALARM
                        putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@AlarmService,
                        alarm.id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    val showIntent = Intent(this@AlarmService, com.example.shakeup.ui.MainActivity::class.java)
                    val showPendingIntent = PendingIntent.getActivity(
                        this@AlarmService,
                        alarm.id.toInt(),
                        showIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setAlarmClock(
                        android.app.AlarmManager.AlarmClockInfo(snoozeMillis, showPendingIntent),
                        pendingIntent
                    )
                }
            }
            _activeAlarmState.value = null
            stopSelf()
        }
    }

    private fun stopAudioAndVibration() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager != null) {
            shakeDetector?.stop(sensorManager)
        }
        shakeDetector = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for active alarm ringing notification"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createFallbackNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Ringing")
            .setContentText("Shake to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()
    }

    private fun createRingingNotification(alarm: Alarm): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarm.id.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            1001,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            1002,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (alarm.label.isNotBlank()) alarm.label else getString(R.string.alarm_ringing_title)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(getString(R.string.shake_to_dismiss_hint))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .addAction(0, getString(R.string.snooze_button), snoozePendingIntent)
            .addAction(0, getString(R.string.dismiss_button), dismissPendingIntent)
            .build()
    }

    override fun onDestroy() {
        stopAudioAndVibration()
        serviceJob.cancel()
        super.onDestroy()
    }

    data class ActiveAlarmData(
        val alarm: Alarm,
        val currentShakes: Int,
        val requiredShakes: Int
    )

    companion object {
        const val CHANNEL_ID = "shakeup_alarm_channel"
        const val NOTIFICATION_ID = 4001

        const val ACTION_START_ALARM = "com.example.shakeup.action.START_ALARM"
        const val ACTION_DISMISS_ALARM = "com.example.shakeup.action.DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.shakeup.action.SNOOZE_ALARM"
        const val EXTRA_ALARM_ID = "com.example.shakeup.extra.ALARM_ID"

        private val _activeAlarmState = MutableStateFlow<ActiveAlarmData?>(null)
        val activeAlarmState: StateFlow<ActiveAlarmData?> = _activeAlarmState.asStateFlow()
    }
}
