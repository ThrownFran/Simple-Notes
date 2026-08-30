## Why

The app depends on `de.raphaelebner:roomdatabasebackup:1.0.0-beta12` purely to move the `AppDatabase` SQLite file in and out of a user-chosen location (backup/restore, wired through `MainActivity`'s drawer menu). That library's `BACKUP_FILE_LOCATION_CUSTOM_DIALOG` mode — the mode this app uses — gates its own SAF file picker behind a runtime permission request (`READ_MEDIA_IMAGES`/`READ_MEDIA_AUDIO`/`READ_MEDIA_VIDEO` on API 33+, `READ_EXTERNAL_STORAGE` on API ≤32), which is why those permissions show up in the merged manifest and in Play Console's Data Safety form even though Simple Notes never touches photos, audio, or video. The underlying operation — "let the user pick a file via the system document picker, then copy bytes" — needs none of that: the Storage Access Framework (`ActivityResultContracts.OpenDocument`/`CreateDocument`) requires zero permissions on any API level this app supports (`minSdk 21`). Dropping the dependency in favor of calling SAF directly removes an unnecessary third-party dependency, removes the unwanted media permissions, and keeps working uniformly from API 21 through the current `targetSdk`.

## What Changes

- Remove the `de.raphaelebner:roomdatabasebackup` Gradle dependency entirely.
- Replace `RoomBackupLib`/`RoomBackupBuilderImp` (in `data/database/backup/`) with a direct Storage Access Framework implementation: `ActivityResultContracts.CreateDocument` for backup (export) and `ActivityResultContracts.OpenDocument` for restore (import), driven from `MainActivity` (SAF pickers require a `ComponentActivity`/`Fragment` registration, same constraint the current `RoomBackupBuilderImp` already has).
- Backup/restore continues to operate on the raw Room database file (`context.getDatabasePath("task_database")`), closing the singleton `AppDatabase` instance before the file copy and relying on the existing post-op app restart (`needsRestartApp`) — matching the current library's behavior, just implemented directly.
- The existing domain-facing abstractions (`RoomBackupHandler`, `RoomBackupBuilder`, `BackupModel`, `BackupAndRestoreRepository`, `BackupAndRestoreNotesUseCase`) are preserved as-is; only their `data/database/backup/` implementation changes. No ViewModel, UseCase, or UI-layer call site changes.
- **BREAKING** (permissions, user-facing): `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, and `READ_EXTERNAL_STORAGE` are dropped from the merged manifest — no runtime permission prompt of any kind precedes backup/restore going forward.

## Capabilities

### New Capabilities
- `notes-backup-restore`: user-initiated export of the notes database to a user-chosen location and import from a user-chosen file, implemented via the Storage Access Framework with no storage/media runtime permissions required, working uniformly across all supported API levels (21+).

### Modified Capabilities
None — no existing `openspec/specs/` capabilities are affected (the `specs/` tree is currently empty; `notes-backup-restore` behavior has never been spec'd before, even though it already exists in code).

## Impact

- **Dependency**: `app/build.gradle` — remove `de.raphaelebner:roomdatabasebackup` and its Room-module exclusions.
- **Code**: `app/src/main/java/brillembourg/notes/simple/data/database/backup/` (`RoomBackupLib.kt`, `RoomBackupHandler.kt`) rewritten to call SAF directly instead of wrapping the library; `di/ActivityScopedModule.kt`, `di/DataSourceModule.kt`, `di/RepositoryModule.kt` updated only as needed to wire the new implementation (interfaces unchanged); `presentation/base/MainActivity.kt` updated to register the SAF `ActivityResultLauncher`s instead of building a `RoomBackup` instance.
- **Manifest**: `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE` disappear from the merged manifest without needing any `tools:node="remove"` override, since nothing declares them anymore.
- **Play Console**: Data Safety / permissions declarations for photo/video/audio access are no longer applicable.
- **Tests**: any existing unit tests around `BackupAndRestoreRepositoryImp`/`RoomBackupHandler` mocks continue to work unchanged (interfaces preserved); no instrumented test coverage exists today for this flow (per `CLAUDE.md`), so verification is manual on-device.
