package brillembourg.notes.simple.domain.usecases.theme

import brillembourg.notes.simple.domain.models.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class SetThemeUseCase
    @Inject
    constructor(
        private val themeManager: ThemeManager,
    ) {
        suspend operator fun invoke(params: Params): Result {
            val result: Deferred<Result> =
                CoroutineScope(coroutineContext).async(Dispatchers.Default) {
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
