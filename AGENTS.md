# Virtue Tracker development

The user requests GitHub version control for this app, named Virtue Tracker (repository virtue-tracker).

- Preserve existing app behavior unless it conflicts with requested changes.
- Keep the opening Overview limited to active tasks. Its only task action is toggling completion via the existing status icon. Task creation, editing, active switching and deletion belong on the Tasks tab/editor.
- Keep the add button fixed-size and outside the scrolling task list.
- Keep task IDs, SharedPreferences name/keys, and completion history compatible. Do not change the Android application ID when renaming the app.
- Verify relevant data and UI behavior before committing/pushing version updates. Upload installable previews with checksums to GitHub releases; never commit APKs, tool caches, credentials or signing keys.
