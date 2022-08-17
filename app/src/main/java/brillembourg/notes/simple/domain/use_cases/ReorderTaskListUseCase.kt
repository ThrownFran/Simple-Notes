package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReorderTaskListUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.reorderTaskList(params)
        }

    class Params(val taskList: List<Task>)
    class Result(val message: String)

}