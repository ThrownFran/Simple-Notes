package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.BackupAndRestoreProvider
import brillembourg.notes.simple.domain.repositories.DataRepository
import brillembourg.notes.simple.domain.use_cases.BackupAndRestoreNotesUseCase
import brillembourg.notes.simple.util.*

class DataRepositoryImp(
    private val backupAndRestoreProvider: BackupAndRestoreProvider
) : DataRepository {

    override suspend fun restore(): Resource<BackupAndRestoreNotesUseCase.Result> {
        return safeCall {
            val result = backupAndRestoreProvider.restoreInLocalStorage()
            if (result.success) {
                Resource.Success(BackupAndRestoreNotesUseCase.Result(UiText.RestoreSuccess))
            } else {
                Resource.Error(RestoreException("Restore error ${result.message}"))
            }
        }
    }

    override suspend fun backup(): Resource<BackupAndRestoreNotesUseCase.Result> {
        return safeCall {
            val result = backupAndRestoreProvider.backupInLocalStorage()
            if (result.success) {
                Resource.Success(BackupAndRestoreNotesUseCase.Result(UiText.BackupSuccess))
            } else {
                Resource.Error(BackupException("Backup error ${result.message}"))
            }
        }
    }

    override suspend fun prepareBackupNotes(params: BackupAndRestoreNotesUseCase.PrepareBackupParams): Resource<BackupAndRestoreNotesUseCase.PrepareBackupResult> {
        return safeCall {
            backupAndRestoreProvider.prepareBackupInLocalStorage(params.screen)
            val message = UiText.DynamicString("")
            Resource.Success(BackupAndRestoreNotesUseCase.PrepareBackupResult(message))
        }
    }
}