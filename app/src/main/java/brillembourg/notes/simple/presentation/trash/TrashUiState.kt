package brillembourg.notes.simple.presentation.trash

import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.models.TaskPresentationModel

data class TrashUiState(
    val taskList: List<TaskPresentationModel> = ArrayList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeState: SelectionModeState = SelectionModeState(),
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowDeleteNotesConfirmationState = ShowDeleteNotesConfirmationState()
)

/*Show confirm to archive notes*/
data class ShowDeleteNotesConfirmationState(
    val isVisible: Boolean = false,
    val tasksToDeleteSize: Int = 0
)

data class NavigateToEditNote(
    val mustConsume: Boolean = false,
    val taskIndex: Int? = null,
    val taskPresentationModel: TaskPresentationModel? = null,
)

/*Notes are selected and contextual bar is shown*/
data class SelectionModeState(
    val isActive: Boolean = false,
    val size: Int = 0
)