package brillembourg.notes.simple.domain.use_cases.cross_categories_notes

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetCategoriesForNoteUseCase @Inject constructor(
    private val repository: NotesRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getCategoriesForNote(params)
            .flowOn(schedulers.defaultDispatcher())
    }

    class Params(val note: Note)
    class Result(val categoryList: List<Category>)
}