package brillembourg.notes.simple.util

import android.content.Context


sealed class UiText {
    data class DynamicString(val value: String) : UiText()

    object GetNotesError : UiText()

    object UnknownError : UiText()
    object BackupSuccess : UiText()
    object BackupFailed : UiText()

    object RestoreSuccess : UiText()
    object RestoreFailed : UiText()

    object NoteCreated : UiText()
    object NoteUpdated : UiText()
    object NotesReordered : UiText()
    object NoteDeleted : UiText()
    object NotesDeleted : UiText()

    object NoteArchived : UiText()
    object NotesArchived : UiText()
    object NoteUnarchived : UiText()
    object NotesUnarchived : UiText()
}


fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.BackupSuccess -> "Backup success"
        is UiText.BackupFailed -> "Backup failed"
        is UiText.NoteArchived -> "Note archived"
        is UiText.NoteCreated -> "Note created"
        is UiText.NoteDeleted -> "Note deleted"
        is UiText.NotesUnarchived -> "Notes unarchived"
        is UiText.NoteUnarchived -> "Note unarchived"
        is UiText.NoteUpdated -> "Note updated"
        is UiText.NotesArchived -> "Notes archived"
        is UiText.NotesDeleted -> "Notes deleted"
        is UiText.NotesReordered -> "Notes reordered"
        is UiText.RestoreFailed -> "Restore failed"
        is UiText.RestoreSuccess -> "Restore success"
        is UiText.UnknownError -> "We are sorry, we got an error!"
        is UiText.GetNotesError -> "Error loading notes"
    }
}


