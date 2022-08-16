package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.repositories.TaskRepository
import javax.inject.Inject

class ArchiveTasksUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend fun execute(params: Params): Result {
        return repository.archiveTasks(params)
    }

    class Params(val ids: List<Long>)
    class Result(val message: String)

}