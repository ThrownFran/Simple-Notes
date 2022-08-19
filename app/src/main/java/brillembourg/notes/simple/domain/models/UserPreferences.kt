package brillembourg.notes.simple.domain.models

data class UserPreferences(val notesLayout: NoteLayout)

enum class NoteLayout {
    Vertical, Grid
}