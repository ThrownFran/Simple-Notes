package brillembourg.notes.simple.domain.use_cases.notes

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.models.NoteWithCategories
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: NotesRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getTaskList(params)
            .flowOn(schedulers.defaultDispatcher())
    }

    data class Params(
        val filterByCategories: List<Category>,
        val keySearch: String = ""
    )

    data class Result(val noteList: List<NoteWithCategories>)
}