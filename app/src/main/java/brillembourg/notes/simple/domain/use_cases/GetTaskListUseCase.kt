package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskListUseCase @Inject constructor(private val repository: TaskRepository) {

    fun execute (params: Params) : Flow<Result> {
        return repository.getTaskList(params)
    }

    class Params ()
    class Result (val taskList: List<Task>)
}