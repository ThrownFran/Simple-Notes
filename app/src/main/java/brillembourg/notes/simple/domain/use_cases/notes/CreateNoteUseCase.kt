package brillembourg.notes.simple.domain.use_cases.notes

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.NoteWithCategories
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val repository: NotesRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.createTask(params)
        }

    class Params(val content: String, val title: String? = null)

    class Result(
        val note: NoteWithCategories,
        val message: UiText
    )

}