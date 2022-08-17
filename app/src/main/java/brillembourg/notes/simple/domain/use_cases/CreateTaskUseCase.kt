package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.createTask(params)
        }

    class Params(val content: String, val title: String? = null)

    class Result(
        val task: Task,
        val message: String
    )

}