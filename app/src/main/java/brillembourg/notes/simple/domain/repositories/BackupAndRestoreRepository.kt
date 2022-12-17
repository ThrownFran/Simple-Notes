package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.notes.BackupAndRestoreNotesUseCase
import brillembourg.notes.simple.util.Resource

interface BackupAndRestoreRepository {
    suspend fun backup(params: BackupAndRestoreNotesUseCase.Params): Resource<BackupAndRestoreNotesUseCase.Result>
    suspend fun restore(params: BackupAndRestoreNotesUseCase.Params): Resource<BackupAndRestoreNotesUseCase.Result>
}