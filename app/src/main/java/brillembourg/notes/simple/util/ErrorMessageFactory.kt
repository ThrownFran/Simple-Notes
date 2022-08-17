package brillembourg.notes.simple.util

fun getMessageFromError(e: Exception): String {
    return when (e) {
        is BackupException -> "Backup failed"
        is RestoreException -> "Restore failed"
        else -> "We are sorry, we got an error!"
    }
}

class BackupException(message: String) : Exception(message)
class RestoreException(message: String) : Exception(message)