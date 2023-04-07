package brillembourg.notes.simple.domain.use_cases.user

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.domain.use_cases.theme.SetThemeUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class GetUserPrefUseCase @Inject constructor(
    private val repository: UserPrefRepository,
    private val setThemeUseCase: SetThemeUseCase,
    private val schedulers: Schedulers
) {

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getUserPreferences(params)
            .onEach {
                if (it is Resource.Success) {
                    setThemeUseCase.invoke(SetThemeUseCase.Params(it.data.preferences.theme))
                }
            }
            .flowOn(schedulers.defaultDispatcher())
    }

    fun asResult(params: Params): Flow<Result> {
        return invoke(params).transform {
            when (it) {
                is Resource.Error -> throw it.exception
                is Resource.Loading -> Unit
                is Resource.Success -> emit(it.data)
            }
        }
    }

    class Params
    class Result(val preferences: UserPreferences)
}