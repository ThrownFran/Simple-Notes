package brillembourg.notes.simple.presentation.home

sealed class HomeState {
    data class ShowError(val message: String) : HomeState()
    object Loading : HomeState()
}