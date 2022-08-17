package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    suspend fun execute(params: Params): Result = with(schedulers.defaultDispatcher()) {
        repository.createTask(params)
    }

    class Params(val content: String, val title: String? = null)

    class Result(
        val task: Task,
        val message: String
    )

}