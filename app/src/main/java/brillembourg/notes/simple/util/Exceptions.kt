package brillembourg.notes.simple.util

fun getMessageFromError(e: Exception): UiText {
    return when (e) {
        is BackupException -> UiText.BackupFailed
        is RestoreException -> UiText.RestoreFailed
        else -> UiText.UnknownError
    }
}

class BackupException(message: String) : Exception(message)
class RestoreException(message: String) : Exception(message)