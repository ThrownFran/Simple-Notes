package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.repositories.BackupAndRestoreRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import javax.inject.Inject

class BackupAndRestoreNotesUseCase @Inject constructor(private val repository: BackupAndRestoreRepository) {

    suspend fun backup(params: Params): Resource<Result> {
        return repository.backup(params)
    }

    suspend fun restore(params: Params): Resource<Result> {
        return repository.restore(params)
    }

    class Result(val message: UiText)
    class Params(val backupModel: BackupModel)
}

//Interface used to represent backupParams in domain layer
interface BackupModel