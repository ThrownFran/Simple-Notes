package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteNotesUseCase @Inject constructor(
    private val repository: NotesRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.deleteTask(params)
        }

    class Params(val ids: List<Long>)
    class Result(val message: UiText)

}