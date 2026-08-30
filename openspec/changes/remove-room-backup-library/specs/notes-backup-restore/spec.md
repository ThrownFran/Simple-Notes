## ADDED Requirements

### Requirement: Users SHALL be able to export the notes database to a location of their choice without any storage or media runtime permission
Triggering "Backup" SHALL open the system's Storage Access Framework document-creation picker directly, with no runtime permission request (of any kind) shown beforehand or required to proceed.

#### Scenario: Successful backup
- **WHEN** the user selects "Backup" from the drawer menu and chooses a destination and filename in the system document picker
- **THEN** the current notes database is copied to the chosen location as a `.sqlite3` file, the user sees a success message, and no permission prompt is shown at any point in the flow

#### Scenario: User cancels the destination picker
- **WHEN** the user selects "Backup" and then dismisses or cancels the system document picker without choosing a destination
- **THEN** no file is written, and the user sees a failure/cancellation message rather than a silent no-op

### Requirement: Users SHALL be able to restore the notes database from a file of their choice without any storage or media runtime permission
Triggering "Restore" SHALL open the system's Storage Access Framework document-opening picker directly, with no runtime permission request shown beforehand or required to proceed.

#### Scenario: Successful restore
- **WHEN** the user selects "Restore" from the drawer menu and chooses a previously created backup file in the system document picker
- **THEN** the notes database is replaced with the contents of the chosen file, the user sees a success message, and no permission prompt is shown at any point in the flow

#### Scenario: User cancels the file picker
- **WHEN** the user selects "Restore" and then dismisses or cancels the system document picker without choosing a file
- **THEN** the existing notes database is left unmodified, and the user sees a failure/cancellation message rather than a silent no-op

#### Scenario: Restoring a backup file created before this change
- **WHEN** the user restores a `.sqlite3` backup file that was produced by the app's previous (library-based) backup implementation
- **THEN** the restore succeeds, since the on-disk backup file format (a raw copy of the Room database file) is unchanged by this change

### Requirement: Backup and restore SHALL behave identically across all Android API levels the app supports
The backup and restore flows SHALL NOT branch on `Build.VERSION.SDK_INT` to request different permission sets, since no permission is required on any supported API level (`minSdk` through `targetSdk`).

#### Scenario: Backup and restore on the minimum supported API level
- **WHEN** the user performs a backup and then a restore on a device running the app's `minSdk` version
- **THEN** both operations complete via the system document picker exactly as they do on a device running the app's `targetSdk` version, with no permission prompt on either

### Requirement: The app SHALL NOT declare storage or media access permissions
The app's merged manifest SHALL NOT contain `android.permission.READ_MEDIA_IMAGES`, `android.permission.READ_MEDIA_AUDIO`, `android.permission.READ_MEDIA_VIDEO`, or `android.permission.READ_EXTERNAL_STORAGE`.

#### Scenario: Merged manifest inspection
- **WHEN** the debug or release merged manifest is generated (e.g. via `./gradlew :app:processDebugMainManifest`)
- **THEN** none of `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, or `READ_EXTERNAL_STORAGE` appear as a `<uses-permission>` entry
