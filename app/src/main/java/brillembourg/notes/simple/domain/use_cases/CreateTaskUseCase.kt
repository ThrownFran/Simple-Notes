package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend fun execute(params: Params): Result {
        return repository.createTask(params)
    }

    class Params(val content: String, val title: String? = null)

    class Result(
        val task: Task,
        val message: String
    )

}