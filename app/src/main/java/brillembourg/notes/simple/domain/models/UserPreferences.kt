package brillembourg.notes.simple.domain.models

import brillembourg.notes.simple.presentation.settings.ThemeMode

data class UserPreferences(
    val _notesLayout: NoteLayout? = null,
    val _theme: ThemeMode? = null
) {
    val noteLayout
        get() = _notesLayout ?: NoteLayout.Vertical
    val theme
        get() = _theme ?: ThemeMode.Light
}

enum class NoteLayout {
    Vertical, Grid
}