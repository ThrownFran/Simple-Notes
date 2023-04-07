package brillembourg.notes.simple.presentation.settings

import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class SavePreferredThemeUseCase @Inject constructor(
    var repository: UserPrefRepository
) {

    suspend operator fun invoke(params: Params): Result {

        val result: Deferred<Result> = CoroutineScope(coroutineContext).async(Dispatchers.IO) {
//            repository.saveTheme(params)
            Result()
        }

        return result.await()
    }

    class Params(var theme: ThemeMode)
    class Result
}