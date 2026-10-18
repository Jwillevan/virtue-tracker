# Virtue Tracker

Offline Android daily tracker, preloaded with Benjamin Franklin's 13 virtues. Native Kotlin and Jetpack Compose; all data stays in the app's existing SharedPreferences store.

## Version 1.1.0

- Opens on **Overview**, a read-only list of active tasks and their daily completion status.
- The **Tasks** tab contains all tasks, including inactive ones. Its + button stays 56 dp regardless of task count, and the list scrolls independently.
- Tap a task to edit its name, description, sub-items (one per line), active switch, and completion for the selected date. Save applies changes; Cancel discards the draft. Delete is available inside the editor with confirmation.
- No edit, delete, completion or activation buttons appear on individual task rows.
- The heading contains only the completed/active-total count, with a proportional progress bar. Inactive tasks do not count toward either number.
- Date browsing, today navigation, the default virtue descriptions, reset behavior, theme and offline storage remain available.

Existing tasks load as active with no sub-items. IDs and completion history are preserved when editing or deactivating; reactivation restores their recorded completion. Deletion removes the task's completion records, as before. Sub-items are editable supporting text, not separate daily completion counters.

## Build and check

Use Java 17 and the Android SDK (platform/build-tools 34). Set JAVA_HOME and ANDROID_HOME, or configure local.properties for the SDK. Do not commit local SDK paths or signing keys.

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

The installable development APK is `app/build/outputs/apk/debug/app-debug.apk`. For a preview and checksum, run `tools/build-preview.ps1`.

With an Android emulator or authorized test device connected:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Tests cover legacy data, sub-items, activation and completion history, deletion/reset, read-only overview, popup editing and the fixed add button with 100 tasks. UI tests use isolated test tasks; run them on an emulator rather than a phone containing real planner data.

## Installation

Download the APK from the GitHub preview release. Its application ID and preferences store are unchanged. This preview uses a local development signing key; updating an existing installation requires the same signing identity. If Android reports a signing conflict, preserve your existing installation and its data while obtaining a build signed with the original key.

The app retains its original Android backup configuration. It has no accounts, network permission or cloud synchronization. Production signing/store publication is separate from the development preview.
