package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

data class HomeUiState(
    val userMessage: UiText? = null,
    val selectionModeState: SelectionModeState = SelectionModeState(),
    val navigateToAddNote: Boolean = false,
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowArchiveNotesConfirmationState = ShowArchiveNotesConfirmationState()
)

/*Show confirm to archive notes*/
data class ShowArchiveNotesConfirmationState(
    val isVisible: Boolean = false,
    val tasksToArchiveSize: Int = 0
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