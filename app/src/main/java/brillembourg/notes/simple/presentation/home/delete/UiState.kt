package brillembourg.notes.simple.presentation.home.delete

import brillembourg.notes.simple.presentation.home.HomeUiState
import brillembourg.notes.simple.presentation.trash.ArchivedUiState
import kotlinx.coroutines.flow.MutableStateFlow

data class UiState(
    val homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState()),
    val trashUiState: MutableStateFlow<ArchivedUiState> = MutableStateFlow(ArchivedUiState())
)