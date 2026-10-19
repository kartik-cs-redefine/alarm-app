# Final Report: ShakeUp (Alarm App)

## 1. Done and Verified by Unit Tests
- **Domain Scheduling Engine (`AlarmCalculator`)**:
  - One-time alarm scheduling (triggers today if in future, tomorrow if in past; disables itself after trigger).
  - Repeating alarm scheduling (reschedules for next matching day of week upon dismiss).
  - Snooze calculation (snoozes for exact specified duration in minutes).
- **Shake Detection Engine (`ShakeProcessor`)**:
  - Net acceleration calculation isolating user movement from gravity (~9.81 m/s²).
  - Debouncing (220ms interval) to ignore rapid acceleration noise.
  - Exact shake count progress tracking and threshold triggering for `EASY` (6 m/s²), `MEDIUM` (10 m/s²), and `HARD` (15 m/s²).
- **Extra Dismiss Challenges**:
  - **Math Challenge (`MathChallengeGenerator`)**: Generates arithmetic equations with computed correct answers.
  - **Step Counter Challenge (`StepProcessor`)**: Tracks step accumulation up to required step count.
- **Data Persistence & Room Database**:
  - `AlarmDao` CRUD operations, order-by-time querying, and TypeConverters for `Set<DayOfWeek>`, `ShakeSensitivity`, and `DismissChallengeType`.
- **UI ViewModels & State Handling**:
  - `AlarmListViewModel`: Handles loading, toggling, and deleting alarms.
  - `AlarmDetailViewModel`: Displays saved alarm time/label when editing, toggles repeat days, updates challenge type, saves and schedules alarms.
  - `AlarmRingingViewModel`: Retains alarm info and handles initial `null` state in `AlarmService.activeAlarmState` without premature activity closure.
- **Build Hardening & Release Shrinking**:
  - R8 minify and resource shrinking enabled in `app/build.gradle.kts`.
  - Custom `proguard-rules.pro` keep rules for Room, Hilt, and Kotlinx Serialization.

## 2. Done but Only Unit-Tested (Not Verified on Physical Hardware)
- Hardware accelerometer sensor listener registration (`SensorManager`).
- Step detector sensor registration (`Sensor.TYPE_STEP_DETECTOR` / `TYPE_STEP_COUNTER`).
- Audio playback (`MediaPlayer`) and hardware vibration (`Vibrator`).
- `AlarmManager.setAlarmClock()` OS system clock triggers and full-screen intent window presentation on physical lock screens.
- Persistent boot broadcast delivery on actual device restart (`BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED`).

## 3. NOT Done
- **QR Code / Barcode Scan Dismiss Challenge**: Skipped as instructed.

## 4. Needs Human Review
- **`AndroidManifest.xml` Declarations**:
  - Permissions: `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `ACTIVITY_RECOGNITION`.
  - Component attributes: `android:windowSoftInputMode="adjustResize"`, `android:exported="true/false"`.
- **Alarm Scheduling & BootReceiver Code**:
  - `AlarmScheduler.kt` (`setAlarmClock` usage) and `BootReceiver.kt` (rescheduling on boot with direct boot safe try-catch).
- **`ACTIVITY_RECOGNITION` Runtime Permission Flow**:
  - Permission needed on Android 10+ (API 29+) for physical step counter sensor usage.
- **Notification Policy (`setBypassDnd`)**:
  - `AlarmService.kt` sets `channel.setBypassDnd(true)`. If the app does not request Do Not Disturb Access (`ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS`), review whether this should be retained or removed per target policy.

## 5. Manual Real-Device Test Checklist
- [ ] **Alarm with Screen Off**: Set an alarm 1 minute in future, turn off screen, and verify device wakes and displays `AlarmRingingActivity`.
- [ ] **After Reboot**: Restart physical device and verify `BootReceiver` reschedules active alarms.
- [ ] **After Snooze**: Tap Snooze during ringing and verify alarm fires again after the configured snooze interval.
- [ ] **Lock Screen Presentation**: Verify ringing UI appears over the lock screen without requiring device unlock.
- [ ] **Shake Dismiss Challenge**: Physically shake device to complete required count and verify alarm stops.
- [ ] **Step Counter Dismiss Challenge**: Walk required steps on a physical device with step sensor to verify step detection dismiss.
- [ ] **Math Challenge**: Enter correct answer to dismiss ringing alarm.
- [ ] **Battery Optimization**: Test alarm reliability on aggressive OEM ROMs (Xiaomi/MIUI, Samsung, Huawei) with background restrictions enabled.

## 6. Known Risks
- **Aggressive OEM Task Killers**: Manufacturers (Xiaomi, Huawei, Samsung, Oppo) may restrict background services unless manually whitelisted in OEM Battery / Autostart settings.
- **Exact Alarm Permission Revocation**: On Android 12+, users can revoke exact alarm permissions in System Settings, which falls back to `setAndAllowWhileIdle()`.
