package brillembourg.notes.simple.util

import brillembourg.notes.simple.presentation.ui_utils.MyLogger

fun getMessageFromError(e: Exception): UiText {
    return when (e) {
        is BackupException -> UiText.BackupFailed
        is RestoreException -> UiText.RestoreFailed
        is GetTaskException -> UiText.GetNotesError
        is GenericException -> UiText.UnknownError
        else -> UiText.UnknownError
    }
}

open class AppException(message: String?) : Exception(message) {
    init {
        MyLogger.record(this)
    }
}

class GenericException(message: String?) : AppException(message)
class BackupException(message: String?) : AppException(message)
class RestoreException(message: String?) : AppException(message)
class GetTaskException(message: String?) : AppException(message)
class GetCategoriesException(message: String?) : AppException(message)