package brillembourg.notes.simple.presentation.trash

import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.models.TaskPresentationModel

data class ArchivedUiState(
    val taskList: List<TaskPresentationModel> = ArrayList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive? = null,
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowDeleteNotesConfirmation? = null
) {

    /*Show confirm to archive notes*/
    data class ShowDeleteNotesConfirmation(
        val tasksToDeleteSize: Int
    )

    data class NavigateToEditNote(
        val mustConsume: Boolean = false,
        val taskIndex: Int? = null,
        val taskPresentationModel: TaskPresentationModel? = null,
    )

    /*Notes are selected and contextual bar is shown*/
    data class SelectionModeActive(
        val size: Int
    )

}

