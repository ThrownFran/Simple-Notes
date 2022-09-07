package brillembourg.notes.simple.presentation.ui_utils

import android.content.Context
import brillembourg.notes.simple.util.UiText

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.BackupSuccess -> "Backup success"
        is UiText.BackupFailed -> "Backup failed"
        is UiText.NoteArchived -> "Note archived"
        is UiText.NoteCreated -> "Note saved"
        is UiText.NoteDeleted -> "Note deleted"
        is UiText.NotesUnarchived -> "Notes unarchived"
        is UiText.NoteUnarchived -> "Note unarchived"
        is UiText.NoteUpdated -> "Note saved"
        is UiText.NotesArchived -> "Notes archived"
        is UiText.NotesDeleted -> "Notes deleted"
        is UiText.NotesReordered -> "Notes reordered"
        is UiText.RestoreFailed -> "Restore failed"
        is UiText.RestoreSuccess -> "Restore success"
        is UiText.UnknownError -> "We are sorry, we got an error!"
        is UiText.GetNotesError -> "Error loading notes"
        is UiText.CategoriesDeleted -> "Labels deleted"
        is UiText.CategoriesReordered -> "Labels reordered"
        is UiText.CategoryCreated -> "Label created"
        is UiText.CategoryDeleted -> "Label deleted"
        is UiText.CategoryUpdated -> "Label updated"
        is UiText.CategoryNameEmpty -> "Label Name is empty"
    }
}