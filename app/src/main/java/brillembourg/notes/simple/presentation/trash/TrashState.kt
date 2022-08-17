package brillembourg.notes.simple.presentation.trash

sealed class TrashState {
    data class ShowError(val message: String) : TrashState()
    object Loading : TrashState()
}