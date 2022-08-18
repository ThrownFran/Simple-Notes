package brillembourg.notes.simple.presentation.base

import brillembourg.notes.simple.util.UiText

data class MainUiState(
    val userToastMessage: UiText? = null,
//    val navigateToCreateTask: Boolean = false,
    val needsRestartApp: Boolean = false
)