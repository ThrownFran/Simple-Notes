package brillembourg.notes.simple.presentation.detail

import brillembourg.notes.simple.util.UiText

data class DetailUiState(
    val title: String? = null,
    val content: String? = null,
    val userMessage: UiText? = null,
    val navigateBack: Boolean = false
)