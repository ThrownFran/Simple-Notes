package brillembourg.notes.simple.domain.models

data class UserPreferences(val notesLayout: NoteLayout = NoteLayout.Vertical)

enum class NoteLayout {
    Vertical, Grid
}