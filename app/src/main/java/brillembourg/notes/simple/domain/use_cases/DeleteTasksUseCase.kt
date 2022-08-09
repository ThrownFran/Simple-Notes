package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteTasksUseCase @Inject constructor(private val repository: TaskRepository) {

    fun execute(params: Params): Flow<Result> {
        return repository.deleteTask(params)
    }

    class Params(val ids: List<Long>)
    class Result(val message: String)

}