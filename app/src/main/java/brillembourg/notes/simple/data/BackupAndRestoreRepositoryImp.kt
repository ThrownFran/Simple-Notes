package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.RoomBackupHandler
import brillembourg.notes.simple.domain.repositories.BackupAndRestoreRepository
import brillembourg.notes.simple.domain.use_cases.BackupAndRestoreNotesUseCase
import brillembourg.notes.simple.util.*

class BackupAndRestoreRepositoryImp(
    private val roomBackupHandler: RoomBackupHandler
) : BackupAndRestoreRepository {

    override suspend fun restore(params: BackupAndRestoreNotesUseCase.Params): Resource<BackupAndRestoreNotesUseCase.Result> {
        return safeCall {
            val result = roomBackupHandler.restoreInLocalStorage(params.backupModel)
            if (result.success) {
                Resource.Success(BackupAndRestoreNotesUseCase.Result(UiText.RestoreSuccess))
            } else {
                Resource.Error(RestoreException("Restore error ${result.message}"))
            }
        }
    }

    override suspend fun backup(params: BackupAndRestoreNotesUseCase.Params): Resource<BackupAndRestoreNotesUseCase.Result> {
        return safeCall {
            val result = roomBackupHandler.backupInLocalStorage(params.backupModel)
            if (result.success) {
                Resource.Success(BackupAndRestoreNotesUseCase.Result(UiText.BackupSuccess))
            } else {
                Resource.Error(BackupException("Backup error ${result.message}"))
            }
        }
    }
}
