package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Screen
import brillembourg.notes.simple.domain.repositories.DataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BackupNotesUseCase @Inject constructor(private val repository: DataRepository) {

    fun backup(): Flow<Result> {
        return repository.backup()
    }

    fun restore(): Flow<Result> {
        return repository.restore()
    }

    class Result(val message: String)

    fun prepareBackup(params: PrepareBackupParams): Flow<PrepareBackupResult> {
        return repository.prepareBackupNotes(params)
    }

    class PrepareBackupParams(val screen: Screen)
    class PrepareBackupResult(val message: String)

}