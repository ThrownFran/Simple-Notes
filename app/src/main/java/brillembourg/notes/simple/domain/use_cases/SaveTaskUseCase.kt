package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    fun execute(params: Params): Flow<Result> {
        return repository.saveTask(params)
    }

    class Params(val id: Long, val content: String)
    class Result(val message: String)

}