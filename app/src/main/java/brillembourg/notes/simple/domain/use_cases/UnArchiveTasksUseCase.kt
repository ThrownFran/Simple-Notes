package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.util.Resource
import javax.inject.Inject

class UnArchiveTasksUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend operator fun invoke(params: Params): Resource<Result> {
        return repository.unArchiveTasks(params)
    }

    class Params(val ids: List<Long>)
    class Result(val message: String)

}