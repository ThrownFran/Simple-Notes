package brillembourg.notes.simple.data.room

import android.content.Context
import brillembourg.notes.simple.domain.use_cases.BackupModel
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RoomBackupLib() : RoomBackupHandler {


    private fun BackupModel.toRoomLibModel(): RoomBackup = (this as BackupWrapperModel).roomBackup

    override suspend fun restoreInLocalStorage(backupModel: BackupModel): RoomBackupHandler.BackupResult {
        return suspendCoroutine {
            val backup = backupModel.toRoomLibModel()

            backup.apply {
                onCompleteListener { success, message, exitCode ->
                    it.resume(RoomBackupHandler.BackupResult(success, message))
                }
            }.restore()
        }
    }

    override suspend fun backupInLocalStorage(backupModel: BackupModel): RoomBackupHandler.BackupResult {
        return suspendCoroutine {
            val backup = backupModel.toRoomLibModel()
            backup.apply {
                onCompleteListener { success, message, exitCode ->
                    it.resume(RoomBackupHandler.BackupResult(success, message))
                }
            }.backup()
        }
    }
}

class BackupWrapperModel(val roomBackup: RoomBackup) : BackupModel

/**
 * This builder requires Activity Context
 */
class RoomBackupBuilderImp(val context: Context) : RoomBackupBuilder {

    override fun prepareBackupInLocalStorage(): BackupModel {
        val roomBackup = RoomBackup(context)
            .database(AppDatabase.invoke(context))
            .enableLogDebug(true)
//            .backupIsEncrypted(true)
//            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .maxFileCount(5)
        return BackupWrapperModel(roomBackup)
    }
}

