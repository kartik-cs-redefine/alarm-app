# Performance & Battery Analysis Notes

> **Note:** Static architecture & code profiling guidelines. Physical device Perfetto traces were not captured in this environment.

## 1. Cold Startup Optimization
- **Application Class (`ShakeUpApp`)**:
  - Annotated with `@HiltAndroidApp`. Heavy background initialization is avoided in `onCreate()`.
  - Room database initialization is deferred until injection on background threads.
- **Composition & Layout**:
  - Unidirectional data flow in Jetpack Compose ensures UI recompositions are minimized to updated state fields (`StateFlow`).
  - Top-level `enableEdgeToEdge()` call in Activity `onCreate()` ensures smooth window inset transitions.

## 2. Battery & Doze Considerations
- **`AlarmManager.setAlarmClock()`**:
  - Wakes the system accurately for alarm trigger without holding continuous background wake locks before the target time.
- **`AlarmService` Foreground Lifecycle**:
  - `WakeLock` and `SensorManager` accelerometer listeners are registered ONLY while the alarm is actively ringing.
  - As soon as the alarm is snoozed or dismissed, `stopAudioAndVibration()` immediately unregisters sensor listeners, releases `MediaPlayer`, cancels `Vibrator`, and terminates `AlarmService` via `stopSelf()`.
- **Aggressive OEM Battery Optimization Guidance**:
  - OEMs (Xiaomi/MIUI, Huawei, Samsung) aggressive background app killers can suppress scheduled alarms or delay broadcast delivery.
  - The app includes an explanatory Card in Settings detailing why battery optimization should be disabled for `com.example.shakeup`.
