# Final Report: ShakeUp (Alarm App)

## What is Done
1. **Architecture & Project Setup**: Built with Kotlin, Jetpack Compose, Material 3, Hilt, Room, DataStore, and type-safe Jetpack Navigation.
2. **Domain & Business Logic**: Alarm models, scheduling calculations (one-time and repeating weekly alarms), shake detection with gravity isolation and debouncing, and math challenge generation.
3. **Data Persistence**: Room database with type converters for days of week, sensitivity, and challenge types; DataStore preferences for default settings.
4. **Alarm Core & Reliability**: `AlarmManager.setAlarmClock()`, exact alarm scheduling, foreground service with media playback ringtone and vibration, full-screen lockscreen activity intent, and `BootReceiver` for device reboot resilience.
5. **UI & Navigation**: Alarm List, Alarm Edit/Detail, Settings, and Full-Screen Ringing activity with pulsing clock animations and real-time shake progress.

## What is Only Unit-Tested (Not Tested on Real Device)
- Hardware accelerometer sensor events (`SensorEvent` via `SensorManager`).
- Audio playback (`MediaPlayer`) and hardware vibration (`Vibrator`).
- `AlarmManager` OS alarm firing and full-screen intent window presentation on physical lockscreens.
- Persistent boot broadcast delivery on device restart (`BOOT_COMPLETED`).

## Manual Device Test Checklist
1. **Alarm with Screen Off**: Set an alarm 1 minute in the future, turn off screen, and verify alarm wakes device and fires full-screen intent.
2. **After Reboot**: Restart device and verify scheduled alarms are successfully re-registered by `BootReceiver`.
3. **After Snooze**: Tap snooze on a ringing alarm and verify it fires again after the snooze interval.
4. **Volume Stream**: Verify alarm sounds on the alarm audio stream even in Doze / DND mode (`setBypassDnd(true)`).
5. **Lock Screen**: Verify ringing activity appears over the keyguard/lock screen.
6. **Shake Dismiss**: Physically shake the device to complete the required shake count and dismiss the alarm.
7. **Battery Optimization**: Verify behavior on aggressive OEM battery savers (Xiaomi/Huawei/Samsung) using the settings battery guide.

## Needs Human Review
- **AndroidManifest.xml**: Permissions (`SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`).
- **Alarm Scheduling & Boot Handling**: Exact alarm clock scheduling and boot broadcast receivers.

## Known Risks
- Aggressive OEM battery management (e.g., MIUI/EMUI/ColorOS) may kill foreground services or restrict exact alarms unless the app is manually whitelisted in battery settings.
- Missing runtime permissions (`SCHEDULE_EXACT_ALARM` on Android 12+) will fallback to inexact/allow-while-idle alarms.
