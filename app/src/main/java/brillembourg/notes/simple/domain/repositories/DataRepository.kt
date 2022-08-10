package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.BackupNotesUseCase
import kotlinx.coroutines.flow.Flow

interface DataRepository {
    fun prepareBackupNotes(params: BackupNotesUseCase.PrepareBackupParams): Flow<BackupNotesUseCase.PrepareBackupResult>
    fun backup(): Flow<BackupNotesUseCase.Result>
    fun restore(): Flow<BackupNotesUseCase.Result>
}