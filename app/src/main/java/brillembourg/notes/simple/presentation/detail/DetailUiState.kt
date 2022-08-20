package brillembourg.notes.simple.presentation.detail

import brillembourg.notes.simple.util.UiText

data class DetailUiState(
    val isNewTask: Boolean = true,
    val isArchivedTask: Boolean = false,
    var userInput: UserInput = UserInput(),
    val userMessage: UiText? = null,
    val navigateBack: Boolean = false,
    val focusInput: Boolean = false,
    val unFocusInput: Boolean = false
)

data class UserInput(val title: String? = null, val content: String? = null) {
    fun isNotEmpty() =
        (title != null && title.isNotEmpty()) || (content != null && content.isNotEmpty())

    fun isNullOrEmpty() = title.isNullOrEmpty() && content.isNullOrEmpty()
}