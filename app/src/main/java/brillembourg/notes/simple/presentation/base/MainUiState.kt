package brillembourg.notes.simple.presentation.base

import brillembourg.notes.simple.util.UiText

data class MainUiState(
    //Normal messages for user
    val userMessage: UiText? = null,
    //Message that survives restart App (for backup/restore)
    val userToastMessage: UiText? = null,

    val needsRestartApp: Boolean = false
)