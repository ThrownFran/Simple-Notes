package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.saveTask(params)
        }

    class Params(val task: Task)
    class Result(val message: UiText)

}