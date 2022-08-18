package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.util.UiText

sealed class HomeState {
    data class ShowError(val message: UiText) : HomeState()
    object Loading : HomeState()
}