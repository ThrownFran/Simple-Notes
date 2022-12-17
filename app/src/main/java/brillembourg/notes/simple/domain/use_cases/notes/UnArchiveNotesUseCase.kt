package brillembourg.notes.simple.domain.use_cases.notes

import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import javax.inject.Inject

class UnArchiveNotesUseCase @Inject constructor(private val repository: NotesRepository) {

    suspend operator fun invoke(params: Params): Resource<Result> {
        return repository.unArchiveTasks(params)
    }

    class Params(val ids: List<Long>)
    class Result(val message: UiText)

}