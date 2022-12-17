package brillembourg.notes.simple.domain.use_cases.notes

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReorderNotesUseCase @Inject constructor(
    private val repository: NotesRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.reorderTaskList(params)
        }

    class Params(val noteList: List<Note>)
    class Result(val message: UiText)

}