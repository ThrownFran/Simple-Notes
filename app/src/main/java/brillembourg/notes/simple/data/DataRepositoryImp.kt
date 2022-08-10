package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.BackupAndRestoreProvider
import brillembourg.notes.simple.domain.repositories.DataRepository
import brillembourg.notes.simple.domain.use_cases.BackupNotesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataRepositoryImp(
    private val backupAndRestoreProvider: BackupAndRestoreProvider
) : DataRepository {

    override fun restore(): Flow<BackupNotesUseCase.Result> {
        return flow {
            val result = backupAndRestoreProvider.restoreInLocalStorage()
            if (result.success) {
                emit(BackupNotesUseCase.Result("Restore success"))
            } else {

                throw Exception("Restore error ${result.message}")
            }
        }
    }

    override fun backup(): Flow<BackupNotesUseCase.Result> {
        return flow {
            val result = backupAndRestoreProvider.backupInLocalStorage()
            if (result.success) {
                emit(BackupNotesUseCase.Result("Backup success"))
            } else {

                throw Exception("Backup error ${result.message}")
            }
        }
    }

    override fun prepareBackupNotes(params: BackupNotesUseCase.PrepareBackupParams): Flow<BackupNotesUseCase.PrepareBackupResult> {
        return flow {
            val result = backupAndRestoreProvider.prepareBackupInLocalStorage(params.screen)
            emit(BackupNotesUseCase.PrepareBackupResult("Backup success"))
        }
    }
}