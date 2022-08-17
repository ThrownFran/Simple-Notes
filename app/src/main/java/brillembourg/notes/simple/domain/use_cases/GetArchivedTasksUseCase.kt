package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetArchivedTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    fun execute(params: Params): Flow<Result> {
        return repository.getArchivedTasks(params)
            .flowOn(schedulers.defaultDispatcher())
    }

    class Params()
    class Result(val taskList: List<Task>)
}