package brillembourg.notes.simple.util

fun getMessageFromError(e: Exception): UiText {
    return when (e) {
        is BackupException -> UiText.BackupFailed
        is RestoreException -> UiText.RestoreFailed
        is GetTaskException -> UiText.GetNotesError
        is GenericException -> UiText.UnknownError
        else -> UiText.UnknownError
    }
}

class GenericException(message: String?) : Exception(message)
class BackupException(message: String?) : Exception(message)
class RestoreException(message: String?) : Exception(message)
class GetTaskException(message: String?) : Exception(message)
class GetCategoriesException(message: String?) : Exception(message)