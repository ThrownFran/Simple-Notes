package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetUserPrefUseCase @Inject constructor(
    private val repository: UserPrefRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getUserPreferences(params)
            .flowOn(schedulers.defaultDispatcher())
    }

    class Params
    class Result(val preferences: UserPreferences)
}