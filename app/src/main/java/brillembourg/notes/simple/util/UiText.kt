package brillembourg.notes.simple.util


sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    object GetNotesError : UiText

    object UnknownError : UiText
    object BackupSuccess : UiText
    object BackupFailed : UiText

    object RestoreSuccess : UiText
    object RestoreFailed : UiText

    object NoteCreated : UiText
    object NoteUpdated : UiText
    object NotesReordered : UiText
    object NoteDeleted : UiText
    object NotesDeleted : UiText

    object NoteArchived : UiText
    object NotesArchived : UiText
    object NoteUnarchived : UiText
    object NotesUnarchived : UiText
    object CategoryCreated : UiText
    object CategoryUpdated : UiText
    object CategoryDeleted : UiText
    object CategoriesDeleted : UiText
    object CategoriesReordered : UiText
    object CategoryNameEmpty : UiText
}


