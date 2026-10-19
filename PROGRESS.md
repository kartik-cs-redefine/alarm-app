# Progress Tracker: ShakeUp

## Phase 0: Baseline
- **Status:** COMPLETED
- **Files Changed:** `.gitignore`, gradle configuration, domain/data/alarm/ui implementation files, test files.
- **Tests Added:** `AlarmCalculatorTest`, `AlarmDaoTest`, `ShakeDetectorTest`, `AlarmListViewModelTest`, `AlarmDetailViewModelTest` (Total 13 unit tests, all passing).
- **Environment Notes:** `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME` conflict. Fix in Windows settings: Remove `ANDROID_PREFS_ROOT` from Windows System Environment Variables so only `ANDROID_USER_HOME` is used.
- **Navigation Library:** Type-safe Jetpack Navigation Compose (`androidx.navigation:navigation-compose`).
- **Open Problems:** None.

## Phase 1: Reliability fixes
- **Status:** COMPLETED
- **Files Changed:** `AlarmService.kt`, `AlarmRingingViewModel.kt`, `MainActivity.kt`, `PROGRESS.md`
- **Tests Added:** Existing unit tests verified and passing.
- **Open Problems:** None.

## Phase 2: Edit screen & BootReceiver
- **Status:** COMPLETED
- **Files Changed:** `AlarmDetailScreen.kt`, `BootReceiver.kt`, `PROGRESS.md`
- **Tests Added:** ViewModel and dao unit tests verified and passing.
- **Open Problems:** None.

## Phase 3: Shake detection
- **Status:** COMPLETED
- **Files Changed:** `ShakeProcessor.kt`, `ShakeDetector.kt`, `ShakeSensitivity.kt`, `ShakeDetectorTest.kt`, `ShakeProcessorTest.kt`, `PROGRESS.md`
- **Tests Added:** `ShakeProcessorTest` (below threshold ignored, debounce logic, exact dismiss count, gravity isolation).
- **Open Problems:** None.

## Phase 4: Strings cleanup
- **Status:** COMPLETED
- **Files Changed:** `strings.xml`, `AlarmListScreen.kt`, `AlarmDetailScreen.kt`, `AlarmRingingScreen.kt`, `PROGRESS.md`
- **Tests Added:** Existing unit tests verified and passing.
- **Open Problems:** None.

## Phase 5: Snooze and repeat correctness
- **Status:** COMPLETED
- **Files Changed:** `AlarmCalculatorTest.kt`, `PROGRESS.md`
- **Tests Added:** `snoozeTimeCalculation` unit test.
- **Open Problems:** None.

## Phase 6: Settings screen
- **Status:** NOT STARTED
- **Files Changed:** None.
- **Tests Added:** None.
- **Open Problems:** None.

## Phase 7: Extra dismiss challenges
- **Status:** NOT STARTED
- **Files Changed:** None.
- **Tests Added:** None.
- **Open Problems:** None.

## Phase 8: UI polish
- **Status:** NOT STARTED
- **Files Changed:** None.
- **Tests Added:** None.
- **Open Problems:** None.

## Phase 9: Hardening
- **Status:** NOT STARTED
- **Files Changed:** None.
- **Tests Added:** None.
- **Open Problems:** None.

## Needs human review
- **AndroidManifest.xml**: Declares `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
- **Alarm scheduling & BootReceiver**: Uses `AlarmManager.setAlarmClock()` and reschedules all enabled alarms on `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED`.
