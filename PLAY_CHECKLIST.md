# Google Play Policy & Permission Compliance Checklist

## 1. Exact Alarm Permission (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`)
- **Declaration in Manifest**: `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` and `<uses-permission android:name="android.permission.USE_EXACT_ALARM" />`.
- **Core Functionality**: Alarms must sound at exact user-specified times.
- **Play Store Requirements**:
  - `USE_EXACT_ALARM` is restricted to Alarm and Timer apps on Play Store. ShakeUp qualifies as a core Alarm application.
  - Runtime check via `AlarmManager.canScheduleExactAlarms()` is implemented before setting exact alarm clocks.
  - UI banner directs users to system settings if exact alarm permission is revoked.

## 2. Full-Screen Intent Permission (`USE_FULL_SCREEN_INTENT`)
- **Declaration in Manifest**: `<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />`.
- **Core Functionality**: Displays high-priority ringing UI (`AlarmRingingActivity`) over lock screen when alarm goes off.
- **Play Store Requirements**:
  - Mandatory requirement for alarm ringing notification priority on Android 14+ (API 34+).
  - High-priority Notification Channel with `CATEGORY_ALARM` and `setFullScreenIntent()` correctly attached.

## 3. Foreground Service Type Declaration (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`)
- **Declaration in Manifest**: `<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />` and `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />`.
- **Service Tag**: `<service android:name=".alarm.AlarmService" android:exported="false" android:foregroundServiceType="mediaPlayback" />`.
- **Play Store Requirements**:
  - `AlarmService` plays alarm ringtone audio in foreground loop until dismissed.
  - Calls `startForeground()` immediately upon `onStartCommand()` to comply with Android 14+ foreground service startup rules.

## 4. Notification Permission (`POST_NOTIFICATIONS`)
- **Declaration in Manifest**: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`.
- **Runtime Flow**: `MainActivity` requests notification permission on Android 13+ (API 33+) with graceful fallback if denied.

## 5. Data Safety & Privacy
- **No Ads, No Tracking**: App collects zero user telemetry, analytics, or personal identifier data.
- **Data Safety Form**: Declare "No data collected, no data shared".
