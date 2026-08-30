## 1. SAF-based backup/restore implementation

- [ ] 1.1 In `data/database/backup/`, implement an `@ActivityScoped` replacement for `RoomBackupBuilderImp` that casts its injected `@ActivityContext Context` to `ComponentActivity` and registers two `ActivityResultLauncher`s during construction (before the host reaches `STARTED`): `ActivityResultContracts.CreateDocument("application/octet-stream")` for backup and `ActivityResultContracts.OpenDocument()` for restore.
- [ ] 1.2 Bridge each launcher's callback into a `suspendCancellableCoroutine` so the class exposes `suspend fun backupInLocalStorage(): RoomBackupHandler.BackupResult` / `suspend fun restoreInLocalStorage(): RoomBackupHandler.BackupResult` (or equivalent), matching the result shape `RoomBackupHandler.BackupResult(success, message)` already used by `BackupAndRestoreRepositoryImp`.
- [ ] 1.3 Implement the backup path: generate filename `$dbName-<timestamp>.sqlite3` (matching the previous library's naming), launch `CreateDocument`, and on a non-null result `Uri`, close the `AppDatabase` singleton, then copy `context.getDatabasePath("task_database")` into `contentResolver.openOutputStream(uri)` on `Dispatchers.IO`; treat a `null` result (user canceled) as a failure result with a clear message.
- [ ] 1.4 Implement the restore path: launch `OpenDocument` filtered to `arrayOf("application/octet-stream")`, and on a non-null result `Uri`, close the `AppDatabase` singleton, then copy `contentResolver.openInputStream(uri)` into `context.getDatabasePath("task_database")` on `Dispatchers.IO`; treat a `null` result (user canceled) as a failure result with a clear message.
- [ ] 1.5 Keep `BackupModel`/`RoomBackupBuilder`/`RoomBackupHandler` interfaces (in `data/database/RoomBackupHandler.kt`) unchanged; `BackupModel` becomes a thin wrapper carrying a reference to the new Activity-scoped SAF class instead of a `RoomBackup` instance.
- [ ] 1.6 Delete `RoomBackupLib.kt` and `RoomBackupBuilderImp` (the old library-backed implementations) once the new classes are in place and wired.

## 2. DI wiring

- [ ] 2.1 Update `di/ActivityScopedModule.kt`'s `backupPrepare` provider to construct the new Activity-scoped SAF implementation instead of `RoomBackupBuilderImp`.
- [ ] 2.2 Update `di/DataSourceModule.kt`'s `backupAndRestore` provider to construct the new `RoomBackupHandler` implementation instead of `RoomBackupLib()`.
- [ ] 2.3 Confirm `di/RepositoryModule.kt`'s `dataRepo` binding needs no changes (it only depends on the unchanged `RoomBackupHandler` interface).
- [ ] 2.4 Confirm `presentation/base/MainActivity.kt` needs no changes to its `backupModel`/`onBackupNotes`/`onRestoreNotes` call sites (interfaces unchanged) beyond what naturally falls out of 1.1–1.5.

## 3. Remove the library dependency

- [ ] 3.1 Remove the `de.raphaelebner:roomdatabasebackup` dependency block (including its `room-guava`/`room-rxjava2` exclusions, now moot) from `app/build.gradle`.
- [ ] 3.2 Run `./gradlew assembleDebug` and confirm the build succeeds with no unresolved references to `de.raphaelebner.roomdatabasebackup.*`.

## 4. Manifest verification

- [ ] 4.1 Run `./gradlew :app:processDebugMainManifest` and inspect `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`, confirming `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, and `READ_EXTERNAL_STORAGE` no longer appear.
- [ ] 4.2 Spot-check `app/build/intermediates/manifest_merge_blame_file/debug/processDebugMainManifest/manifest-merger-blame-debug-report.txt` to confirm no other dependency reintroduces any of those four permissions.

## 5. Manual verification (no instrumented coverage exists for this flow)

- [ ] 5.1 On an emulator/device, perform a backup via the drawer menu: confirm the system document picker opens directly with no permission dialog, choose a destination, and confirm the success toast/message appears and the file is written.
- [ ] 5.2 Modify notes after the backup, then perform a restore selecting the file from 5.1: confirm the system document picker opens directly with no permission dialog, confirm the success message, confirm the app restarts, and confirm notes reflect the restored (pre-modification) state.
- [ ] 5.3 Restore a `.sqlite3` backup file previously produced by the old library-based implementation (if one is available from before this change), confirming format compatibility.
- [ ] 5.4 Cancel the picker mid-flow for both backup and restore, confirming a failure/cancellation message is shown rather than a silent no-op or crash.
- [ ] 5.5 Repeat 5.1–5.2 on both a low-API (≤32) and high-API (33+) emulator image if both are available, confirming identical behavior with no SDK-version-dependent permission prompts on either.

## 6. Documentation

- [ ] 6.1 Update `CLAUDE.md` if it references the `roomdatabasebackup` dependency or backup/restore implementation details that are now stale.
