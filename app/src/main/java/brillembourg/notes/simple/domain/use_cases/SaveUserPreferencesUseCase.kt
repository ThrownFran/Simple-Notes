package brillembourg.notes.simple.domain.use_cases

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveUserPreferencesUseCase @Inject constructor(
    private val repository: UserPrefRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params) = withContext(schedulers.defaultDispatcher()) {
        repository.saveUserPreferences(params)
    }

    class Params(val userPreferences: UserPreferences)
}