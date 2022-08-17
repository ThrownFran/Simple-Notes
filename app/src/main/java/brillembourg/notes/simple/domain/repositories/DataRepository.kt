package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.BackupNotesUseCase
import brillembourg.notes.simple.util.Resource

interface DataRepository {
    suspend fun prepareBackupNotes(params: BackupNotesUseCase.PrepareBackupParams): Resource<BackupNotesUseCase.PrepareBackupResult>
    suspend fun backup(): Resource<BackupNotesUseCase.Result>
    suspend fun restore(): Resource<BackupNotesUseCase.Result>
}