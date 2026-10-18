# Virtue Tracker 1.1.0 preview

- Overview opens first and is read-only, showing active tasks and their completion status.
- The second tab, Tasks, contains all tasks and a fixed-size + button that does not shrink as the list grows.
- Tap a task to open its editor for name, description, sub-items (one per line), active/inactive switch, completion for the selected date, and deletion. Task rows have no action buttons.
- The heading shows only completed/active-total counts and a proportional progress bar.
- Existing task IDs, daily completion history, default virtues/precepts, date browsing, reset behavior, theme and local storage are preserved. Older tasks default to active; deactivation retains history. Sub-items are supporting text rather than separate completion counters.
- The app and repository are named Virtue Tracker / virtue-tracker.

Validation: 3 data/history tests and 2 Android emulator UI tests passed. UI checks include 100 tasks, fixed add-button dimensions, popup editing, active-only overview, completion progress and deletion. Android lint completed without errors (older SDK/dependency warnings remain). The app built, installed and launched on Android 16; the Overview layout was visually inspected.

Download the APK and checksum below. This is a development-signed preview, not a store release. An in-place update of an existing app requires its original signing identity; if Android reports a signature conflict, keep the installed app and data while obtaining a build signed with the original key.
