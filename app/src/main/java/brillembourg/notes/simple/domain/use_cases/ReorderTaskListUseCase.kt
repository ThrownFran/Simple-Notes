package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReorderTaskListUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    suspend fun execute(params: Params): Result = withContext(schedulers.defaultDispatcher()) {
        repository.reorderTaskList(params)
    }

    class Params(val taskList: List<Task>)
    class Result(val message: String)

}