package brillembourg.notes.simple.util

/**
 * Manage data handling in a clean way. Extracte from philipplackner
 * https://github.com/philipplackner/StockMarketApp/blob/final/app/src/main/java/com/plcoding/stockmarketapp/util/Resource.kt
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(data: T, message: String? = null) : Resource<T>(data, message)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true) : Resource<T>()

}