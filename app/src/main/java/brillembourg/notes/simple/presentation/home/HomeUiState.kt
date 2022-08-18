package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

data class HomeUiState(
    val userMessage: UiText? = null,
    var navigateToDetail: NavigateToTaskDetailEvent = NavigateToTaskDetailEvent(false)
)

data class NavigateToTaskDetailEvent(
    val mustConsume: Boolean = false,
    val taskIndex: Int? = null,
    val taskPresentationModel: TaskPresentationModel? = null,
)