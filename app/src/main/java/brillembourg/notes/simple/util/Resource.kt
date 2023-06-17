package brillembourg.notes.simple.util

/**
 * Data handling in a clean way. Extracted from philipplackner
 * https://github.com/philipplackner/StockMarketApp/blob/final/app/src/main/java/com/plcoding/stockmarketapp/util/Resource.kt
 *
 * Modification: Error class has exception instead of message to increase flexibility in the UI
 */
sealed interface Resource<out T> {
    class Success<T>(val data: T) : Resource<T>
    class Error<T>(val exception: Exception, val data: T? = null) : Resource<T>
    class Loading<T> : Resource<T>
}

suspend fun <T> safeCall(block: suspend () -> Resource<T>): Resource<T> {
    return try {
        block.invoke()
    } catch (e: Exception) {
        Resource.Error(GenericException(e.message))
    }
}