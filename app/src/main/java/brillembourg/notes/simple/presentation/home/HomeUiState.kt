package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

data class HomeUiState(
    val noteList: NoteList = NoteList(),
    val userMessage: UserMessage = UserMessage(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeState: SelectionModeState = SelectionModeState(),
    val navigateToAddNote: Boolean = false,
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowArchiveNotesConfirmationState = ShowArchiveNotesConfirmationState()
)

data class UserMessage(
    val isShowing: Boolean = false,
    val currentMessage: UiText? = null,
    val prevMessage: UiText? = null
) {
    fun mustRenderMessage(): Boolean {
        return currentMessage != null && !isShowing
    }
}

data class NoteList(
    val notes: List<TaskPresentationModel> = ArrayList(),
    val mustRender: Boolean = false //To avoid redrawing list in every status change
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