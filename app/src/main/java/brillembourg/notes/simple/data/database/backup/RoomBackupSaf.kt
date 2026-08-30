package brillembourg.notes.simple.data.database.backup

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import brillembourg.notes.simple.data.database.AppDatabase
import brillembourg.notes.simple.data.database.RoomBackupBuilder
import brillembourg.notes.simple.data.database.RoomBackupHandler
import brillembourg.notes.simple.domain.usecases.notes.BackupModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Backs up and restores [AppDatabase]'s raw SQLite file through the Storage Access Framework
 * (system document picker), requiring no storage/media runtime permission on any API level.
 *
 * The picker launchers must be registered before [activity] reaches `STARTED`, so this class is
 * built eagerly from [RoomBackupBuilderImp], which is constructed during Hilt's Activity field
 * injection (inside `super.onCreate()`, before the host Activity's own `onCreate` body runs).
 */
class RoomBackupSafSession(private val activity: ComponentActivity) {
    private val context: Context get() = activity.applicationContext

    private var pendingBackup: ((Uri?) -> Unit)? = null
    private var pendingRestore: ((Uri?) -> Unit)? = null

    private val createDocumentLauncher =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            pendingBackup?.invoke(uri)
            pendingBackup = null
        }

    private val openDocumentLauncher =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            pendingRestore?.invoke(uri)
            pendingRestore = null
        }

    suspend fun backup(): RoomBackupHandler.BackupResult {
        val dbName = AppDatabase.invoke(context).openHelper.databaseName ?: DEFAULT_DATABASE_NAME
        val uri =
            suspendCancellableCoroutine<Uri?> { continuation ->
                pendingBackup = { uri -> continuation.resume(uri) }
                createDocumentLauncher.launch(backupFileName(dbName))
            } ?: return RoomBackupHandler.BackupResult(false, "Backup canceled, no destination chosen")

        return withContext(Dispatchers.IO) {
            try {
                AppDatabase.invoke(context).close()
                val output =
                    context.contentResolver.openOutputStream(uri)
                        ?: return@withContext RoomBackupHandler.BackupResult(false, "Could not open output stream for $uri")
                output.use { out ->
                    context.getDatabasePath(dbName).inputStream().use { input -> input.copyTo(out) }
                }
                RoomBackupHandler.BackupResult(true, "success")
            } catch (e: Exception) {
                RoomBackupHandler.BackupResult(false, "Backup error: ${e.message}")
            }
        }
    }

    suspend fun restore(): RoomBackupHandler.BackupResult {
        val dbName = AppDatabase.invoke(context).openHelper.databaseName ?: DEFAULT_DATABASE_NAME
        val uri =
            suspendCancellableCoroutine<Uri?> { continuation ->
                pendingRestore = { uri -> continuation.resume(uri) }
                openDocumentLauncher.launch(arrayOf("application/octet-stream"))
            } ?: return RoomBackupHandler.BackupResult(false, "Restore canceled, no file chosen")

        return withContext(Dispatchers.IO) {
            try {
                AppDatabase.invoke(context).close()
                val input =
                    context.contentResolver.openInputStream(uri)
                        ?: return@withContext RoomBackupHandler.BackupResult(false, "Could not open input stream for $uri")
                input.use { inp ->
                    context.getDatabasePath(dbName).outputStream().use { output -> inp.copyTo(output) }
                }
                RoomBackupHandler.BackupResult(true, "success")
            } catch (e: Exception) {
                RoomBackupHandler.BackupResult(false, "Restore error: ${e.message}")
            }
        }
    }

    private fun backupFileName(dbName: String): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        return "$dbName-$timestamp.sqlite3"
    }

    companion object {
        private const val DEFAULT_DATABASE_NAME = "task_database"
    }
}

class SafBackupModel(val session: RoomBackupSafSession) : BackupModel

class RoomBackupSafHandler : RoomBackupHandler {
    private fun BackupModel.session() = (this as SafBackupModel).session

    override suspend fun restoreInLocalStorage(backupModel: BackupModel): RoomBackupHandler.BackupResult = backupModel.session().restore()

    override suspend fun backupInLocalStorage(backupModel: BackupModel): RoomBackupHandler.BackupResult = backupModel.session().backup()
}

/**
 * This builder requires Activity Context
 */
class RoomBackupBuilderImp(private val context: Context) : RoomBackupBuilder {
    override fun prepareBackupInLocalStorage(): BackupModel = SafBackupModel(RoomBackupSafSession(context as ComponentActivity))
}
