package brillembourg.notes.simple.data.room

import android.content.Context
import brillembourg.notes.simple.domain.ContextDomain
import brillembourg.notes.simple.domain.Screen
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RoomBackupLib(val context: Context) : BackupAndRestoreProvider {

    var backup: RoomBackup? = null

    override suspend fun restoreInLocalStorage(): BackupAndRestoreProvider.BackupResult {
        return suspendCoroutine {
            if (backup == null) throw IllegalArgumentException("Backup has not been prepared. (OnCreate in Activity)")

            backup?.apply {
                onCompleteListener { success, message, exitCode ->
                    it.resume(BackupAndRestoreProvider.BackupResult(success, message))
//                    backup = null
                }
            }?.restore()
        }
    }

    override suspend fun backupInLocalStorage(): BackupAndRestoreProvider.BackupResult {
        return suspendCoroutine {
            if (backup == null) throw IllegalArgumentException("Backup has not been prepared. (OnCreate in Activity)")

            backup?.apply {
                onCompleteListener { success, message, exitCode ->
                    it.resume(BackupAndRestoreProvider.BackupResult(success, message))
                    backup = null
                }
            }?.backup()
        }
    }

    override fun prepareBackupInLocalStorage(screen: Screen) {
        val context = (screen as ContextDomain).context
        backup = RoomBackup(context)
            .database(AppDatabase.Companion.invoke(context))
            .enableLogDebug(true)
//            .backupIsEncrypted(true)
//            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .maxFileCount(5)
    }


}