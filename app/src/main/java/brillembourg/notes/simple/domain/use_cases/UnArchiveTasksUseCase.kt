package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.repositories.TaskRepository
import javax.inject.Inject

class UnArchiveTasksUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend fun execute(params: Params): Result {
        return repository.unArchiveTasks(params)
    }

    class Params(val ids: List<Long>)
    class Result(val message: String)

}