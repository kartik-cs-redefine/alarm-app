# Progress Tracker: ShakeUp

## Step 0: Reality Check
- **Git Commit Log:** 10 commits present (`Phase 0` through `Phase 9`).
- **Test Count:** 19 unit tests across 7 test files (`ShakeDetectorTest.kt`, `ShakeProcessorTest.kt`, `AlarmDaoTest.kt`, `AlarmCalculatorTest.kt`, `MathChallengeTest.kt`, `AlarmDetailViewModelTest.kt`, `AlarmListViewModelTest.kt`), all passing.

## Item 1: Phase 8 UI Polish
- **Status:** COMPLETED
- **Files Changed:** `AndroidManifest.xml` (`windowSoftInputMode="adjustResize"`), `MainActivity.kt` (Edge-to-edge support & two-pane adaptive layout for tablets/foldables when `maxWidth >= 600.dp`), `PROGRESS.md`.
- **Theme Changes:** Confirmed Material 3 `ShakeUpTheme` uses dynamic color on Android 12+ and system dark theme.
- **Tests Added/Passing:** All 19 unit tests passing.
- **Open Problems:** None.

## Item 2: Phase 9 Hardening
- **Status:** NOT STARTED

## Item 3: Missing Tests
- **Status:** NOT STARTED

## Item 4: Steps Counter Dismiss Challenge
- **Status:** NOT STARTED

## Item 5: Shake Thresholds Comment
- **Status:** NOT STARTED

## Item 6: Honest FINAL_REPORT.md
- **Status:** NOT STARTED

## Needs human review
- **AndroidManifest.xml**: Declares `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `windowSoftInputMode="adjustResize"`.
- **Alarm scheduling & BootReceiver**: Uses `AlarmManager.setAlarmClock()` and reschedules all enabled alarms on `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED`.
