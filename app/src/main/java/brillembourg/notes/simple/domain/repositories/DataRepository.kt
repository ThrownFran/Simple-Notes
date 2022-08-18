package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.BackupAndRestoreNotesUseCase
import brillembourg.notes.simple.util.Resource

interface DataRepository {
    suspend fun prepareBackupNotes(params: BackupAndRestoreNotesUseCase.PrepareBackupParams): Resource<BackupAndRestoreNotesUseCase.PrepareBackupResult>
    suspend fun backup(): Resource<BackupAndRestoreNotesUseCase.Result>
    suspend fun restore(): Resource<BackupAndRestoreNotesUseCase.Result>
}