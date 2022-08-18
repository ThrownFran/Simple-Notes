package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.BackupAndRestoreProvider
import brillembourg.notes.simple.domain.repositories.DataRepository
import brillembourg.notes.simple.domain.use_cases.BackupNotesUseCase
import brillembourg.notes.simple.util.*

class DataRepositoryImp(
    private val backupAndRestoreProvider: BackupAndRestoreProvider
) : DataRepository {

    override suspend fun restore(): Resource<BackupNotesUseCase.Result> {
        return safeCall {
            val result = backupAndRestoreProvider.restoreInLocalStorage()
            if (result.success) {
                Resource.Success(BackupNotesUseCase.Result(UiText.RestoreSuccess))
            } else {
                Resource.Error(RestoreException("Restore error ${result.message}"))
            }
        }
    }

    override suspend fun backup(): Resource<BackupNotesUseCase.Result> {
        return safeCall {
            val result = backupAndRestoreProvider.backupInLocalStorage()
            if (result.success) {
                Resource.Success(BackupNotesUseCase.Result(UiText.BackupSuccess))
            } else {
                Resource.Error(BackupException("Backup error ${result.message}"))
            }
        }
    }

    override suspend fun prepareBackupNotes(params: BackupNotesUseCase.PrepareBackupParams): Resource<BackupNotesUseCase.PrepareBackupResult> {
        return safeCall {
            backupAndRestoreProvider.prepareBackupInLocalStorage(params.screen)
            val message = UiText.DynamicString("")
            Resource.Success(BackupNotesUseCase.PrepareBackupResult(message))
        }
    }
}