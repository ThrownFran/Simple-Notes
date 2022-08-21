package brillembourg.notes.simple.data.room

import brillembourg.notes.simple.domain.use_cases.Screen


interface BackupAndRestoreProvider {

    class BackupResult(
        val success: Boolean,
        val message: String
    )

    suspend fun restoreInLocalStorage(): BackupResult

    fun prepareBackupInLocalStorage(screen: Screen)
    suspend fun backupInLocalStorage(): BackupResult
}