package brillembourg.notes.simple.presentation.settings

import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class SetThemeUseCase @Inject constructor(
    private val themeManager: ThemeManager
) {

    suspend operator fun invoke(params: Params): Result {

        val result: Deferred<Result> = CoroutineScope(coroutineContext).async(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                themeManager.changeTheme(params.currentThemeMode)
            }
            Result()
        }

        return result.await()
    }

    class Params(val currentThemeMode: ThemeMode)
    class Result
}