package brillembourg.notes.simple.domain.use_cases.user

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveFilterByCategoriesUseCase @Inject constructor(
    private val repository: UserPrefRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params) = withContext(schedulers.defaultDispatcher()) {
        repository.saveFilter(params)
    }

    class Params(val categoryIds: List<Long>)
}