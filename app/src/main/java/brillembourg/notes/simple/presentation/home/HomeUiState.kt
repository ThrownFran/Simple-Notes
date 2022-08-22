package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.models.NotePresentationModel

data class HomeUiState(
    val noteList: NoteList = NoteList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive? = null,
    val navigateToAddNote: Boolean = false,
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowArchiveNotesConfirmationState? = null
)

data class NoteList(
    val notes: List<NotePresentationModel> = ArrayList(),
    val mustRender: Boolean = false //To avoid rendering set false
)

/*Show confirm to archive notes*/
data class ShowArchiveNotesConfirmationState(
    val tasksToArchiveSize: Int
)

data class NavigateToEditNote(
    val mustConsume: Boolean = false,
    val taskIndex: Int? = null,
    val notePresentationModel: NotePresentationModel? = null,
)

/*Notes are selected and contextual bar is shown*/
data class SelectionModeActive(
    val size: Int
)