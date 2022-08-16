package brillembourg.notes.simple.ui.trash

sealed class TrashState {
    data class ShowError(val message: String) : TrashState()
    object Loading : TrashState()
}