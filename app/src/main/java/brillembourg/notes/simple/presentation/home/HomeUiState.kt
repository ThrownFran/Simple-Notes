package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

data class HomeUiState(
    val selectionModeState: SelectionModeState = SelectionModeState(),
    val userMessage: UiText? = null,
    val navigateToAddNote: Boolean = false,
    var navigateToDetail: NavigateToTaskDetail = NavigateToTaskDetail(false)
)

data class NavigateToTaskDetail(
    val mustConsume: Boolean = false,
    val taskIndex: Int? = null,
    val taskPresentationModel: TaskPresentationModel? = null,
)

data class SelectionModeState(
    val isActive: Boolean = false,
    val size: Int = 0
)