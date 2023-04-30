package brillembourg.notes.simple.presentation.home.delete

import brillembourg.notes.simple.presentation.home.HomeUiState
import brillembourg.notes.simple.presentation.home.NoteList
import brillembourg.notes.simple.presentation.trash.ArchivedUiState
import kotlinx.coroutines.flow.MutableStateFlow

data class UiState(
    val homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState()),
    val noteList: MutableStateFlow<NoteList> = MutableStateFlow(NoteList()),
    val trashUiState: MutableStateFlow<ArchivedUiState> = MutableStateFlow(ArchivedUiState())
)