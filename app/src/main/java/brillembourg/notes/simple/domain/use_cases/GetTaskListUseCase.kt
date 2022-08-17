package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetTaskListUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getTaskList(params)
            .flowOn(schedulers.defaultDispatcher())
    }

    class Params
    class Result(val taskList: List<Task>)
}