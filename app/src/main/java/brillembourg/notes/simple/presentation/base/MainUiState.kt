package brillembourg.notes.simple.presentation.base

import brillembourg.notes.simple.util.UiText

data class MainUiState(
    val userContextMessage: UiText? = null,
    val needsRestartApp: Boolean = false
)