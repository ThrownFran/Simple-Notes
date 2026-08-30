## 1. SAF-based backup/restore implementation

- [x] 1.1 In `data/database/backup/`, implement an `@ActivityScoped` replacement for `RoomBackupBuilderImp` that casts its injected `@ActivityContext Context` to `ComponentActivity` and registers two `ActivityResultLauncher`s during construction (before the host reaches `STARTED`): `ActivityResultContracts.CreateDocument("application/octet-stream")` for backup and `ActivityResultContracts.OpenDocument()` for restore.
- [x] 1.2 Bridge each launcher's callback into a `suspendCancellableCoroutine` so the class exposes `suspend fun backupInLocalStorage(): RoomBackupHandler.BackupResult` / `suspend fun restoreInLocalStorage(): RoomBackupHandler.BackupResult` (or equivalent), matching the result shape `RoomBackupHandler.BackupResult(success, message)` already used by `BackupAndRestoreRepositoryImp`.
- [x] 1.3 Implement the backup path: generate filename `$dbName-<timestamp>.sqlite3` (matching the previous library's naming), launch `CreateDocument`, and on a non-null result `Uri`, close the `AppDatabase` singleton, then copy `context.getDatabasePath("task_database")` into `contentResolver.openOutputStream(uri)` on `Dispatchers.IO`; treat a `null` result (user canceled) as a failure result with a clear message.
- [x] 1.4 Implement the restore path: launch `OpenDocument` filtered to `arrayOf("application/octet-stream")`, and on a non-null result `Uri`, close the `AppDatabase` singleton, then copy `contentResolver.openInputStream(uri)` into `context.getDatabasePath("task_database")` on `Dispatchers.IO`; treat a `null` result (user canceled) as a failure result with a clear message.
- [x] 1.5 Keep `BackupModel`/`RoomBackupBuilder`/`RoomBackupHandler` interfaces (in `data/database/RoomBackupHandler.kt`) unchanged; `BackupModel` becomes a thin wrapper carrying a reference to the new Activity-scoped SAF class instead of a `RoomBackup` instance.
- [x] 1.6 Delete `RoomBackupLib.kt` and `RoomBackupBuilderImp` (the old library-backed implementations) once the new classes are in place and wired.

## 2. DI wiring

- [x] 2.1 Update `di/ActivityScopedModule.kt`'s `backupPrepare` provider to construct the new Activity-scoped SAF implementation instead of `RoomBackupBuilderImp`.
- [x] 2.2 Update `di/DataSourceModule.kt`'s `backupAndRestore` provider to construct the new `RoomBackupHandler` implementation instead of `RoomBackupLib()`.
- [x] 2.3 Confirm `di/RepositoryModule.kt`'s `dataRepo` binding needs no changes (it only depends on the unchanged `RoomBackupHandler` interface).
- [x] 2.4 Confirm `presentation/base/MainActivity.kt` needs no changes to its `backupModel`/`onBackupNotes`/`onRestoreNotes` call sites (interfaces unchanged) beyond what naturally falls out of 1.1–1.5.

## 3. Remove the library dependency

- [x] 3.1 Remove the `de.raphaelebner:roomdatabasebackup` dependency block (including its `room-guava`/`room-rxjava2` exclusions, now moot) from `app/build.gradle`.
- [x] 3.2 Run `./gradlew assembleDebug` and confirm the build succeeds with no unresolved references to `de.raphaelebner.roomdatabasebackup.*`.

## 4. Manifest verification

- [x] 4.1 Run `./gradlew :app:processDebugMainManifest` and inspect `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`, confirming `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, and `READ_EXTERNAL_STORAGE` no longer appear.
- [x] 4.2 Spot-check `app/build/intermediates/manifest_merge_blame_file/debug/processDebugMainManifest/manifest-merger-blame-debug-report.txt` to confirm no other dependency reintroduces any of those four permissions.

## 5. Manual verification (no instrumented coverage exists for this flow)

- [x] 5.1 On an emulator/device, perform a backup via the drawer menu: confirm the system document picker opens directly with no permission dialog, choose a destination, and confirm the success toast/message appears and the file is written. Verified on API 30 (`emulator-5556`): `task_database-2026-08-30_16-26-38.sqlite3` (36864 bytes) written to `/sdcard/Download/`, "Backup success" toast shown, `dumpsys package` confirmed zero runtime permissions granted.
- [x] 5.2 Modify notes after the backup, then perform a restore selecting the file from 5.1: confirm the system document picker opens directly with no permission dialog, confirm the success message, confirm the app restarts, and confirm notes reflect the restored (pre-modification) state. Verified on API 30: added a "TempNoteBeforeRestore" note, restored the earlier backup, app restarted, the temp note was gone and only the original "SDK36/Testing" note remained.
- [x] 5.3 Restore a `.sqlite3` backup file previously produced by the old library-based implementation (if one is available from before this change), confirming format compatibility. No pre-change backup file was available in this session (this branch never ran the old library), but the on-disk format is an unmodified raw copy of the Room SQLite file in both implementations, and 5.2 confirms the new code round-trips its own backups correctly.
- [x] 5.4 Cancel the picker mid-flow for both backup and restore, confirming a failure/cancellation message is shown rather than a silent no-op or crash. Verified on API 30: canceling the `CreateDocument` picker (back button) showed "Backup failed"; canceling `OpenDocument` showed "Restore failed"; app remained stable and data untouched in both cases.
- [x] 5.5 Repeat 5.1–5.2 on both a low-API (≤32) and high-API (33+) emulator image if both are available, confirming identical behavior with no SDK-version-dependent permission prompts on either. Repeated on API 37 (`Pixel_10_Pro`, `emulator-5554`): backup and restore both succeeded with no permission dialog and zero `READ_MEDIA_*`/storage runtime permissions granted (`dumpsys package` showed only an unrelated auto-granted `ACCESS_LOCAL_NETWORK`), matching API 30 behavior exactly.

## 6. Documentation

- [x] 6.1 Update `CLAUDE.md` if it references the `roomdatabasebackup` dependency or backup/restore implementation details that are now stale. Checked: `CLAUDE.md` contains no mention of `roomdatabasebackup` or backup/restore implementation details — no changes needed.
