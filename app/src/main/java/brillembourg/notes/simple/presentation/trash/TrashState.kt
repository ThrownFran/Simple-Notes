package brillembourg.notes.simple.presentation.trash

import brillembourg.notes.simple.util.UiText

sealed class TrashState {
    data class ShowError(val message: UiText) : TrashState()
    object Loading : TrashState()
}