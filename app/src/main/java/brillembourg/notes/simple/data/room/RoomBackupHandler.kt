package brillembourg.notes.simple.data.room

import brillembourg.notes.simple.domain.use_cases.BackupModel


interface RoomBackupHandler {
    suspend fun restoreInLocalStorage(backupModel: BackupModel): BackupResult
    suspend fun backupInLocalStorage(backupModel: BackupModel): BackupResult

    class BackupResult(
        val success: Boolean,
        val message: String
    )
}

interface RoomBackupBuilder {
    fun prepareBackupInLocalStorage(): BackupModel
}