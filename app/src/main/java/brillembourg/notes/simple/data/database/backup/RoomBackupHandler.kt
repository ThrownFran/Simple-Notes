package brillembourg.notes.simple.data.database

import brillembourg.notes.simple.domain.use_cases.notes.BackupModel


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