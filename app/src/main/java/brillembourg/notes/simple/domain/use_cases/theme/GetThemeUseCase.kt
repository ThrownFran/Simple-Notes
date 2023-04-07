package brillembourg.notes.simple.domain.use_cases.theme

import brillembourg.notes.simple.domain.models.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class GetThemeUseCase @Inject constructor(
    private val themeManager: ThemeManager,
) {

    suspend operator fun invoke(): Result {

        val result: Deferred<Result> = CoroutineScope(coroutineContext).async(Dispatchers.IO) {
            Result(themeManager.themeList)
        }
        return result.await()
    }

    class Params
    class Result(
        var themeList: List<ThemeMode>
    )
}