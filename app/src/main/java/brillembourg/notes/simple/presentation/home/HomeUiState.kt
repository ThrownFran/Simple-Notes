package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

data class HomeUiState(
    val selectionMode: SelectionMode = SelectionMode(),
//    var isSelectionMode: Boolean = false,
    val userMessage: UiText? = null,
    var navigateToDetail: NavigateToTaskDetailEvent = NavigateToTaskDetailEvent(false)
)

data class NavigateToTaskDetailEvent(
    val mustConsume: Boolean = false,
    val taskIndex: Int? = null,
    val taskPresentationModel: TaskPresentationModel? = null,
)

data class SelectionMode(
    val isActive: Boolean = false,
    val size: Int = 0
)