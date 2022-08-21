package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.repositories.BackupAndRestoreRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import javax.inject.Inject

class BackupAndRestoreNotesUseCase @Inject constructor(private val repository: BackupAndRestoreRepository) {

    suspend fun backup(): Resource<Result> {
        return repository.backup()
    }

    suspend fun restore(): Resource<Result> {
        return repository.restore()
    }

    class Result(val message: UiText)

    suspend fun prepareBackup(params: PrepareBackupParams): Resource<PrepareBackupResult> {
        return repository.prepareBackupNotes(params)
    }

    class PrepareBackupParams(val screen: Screen)
    class PrepareBackupResult(val message: UiText)
}

//Interface used to represent UI in domain layer
interface Screen