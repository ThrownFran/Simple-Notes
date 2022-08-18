package brillembourg.notes.simple.presentation.home

import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

data class HomeUiState(
    val userMessage: UiText? = null,
    var navigateToDetail: NavigateToDetailEvent = NavigateToDetailEvent(false)
)

data class NavigateToDetailEvent(
    val mustConsume: Boolean,
    val taskIndex: Int? = null,
    val taskPresentationModel: TaskPresentationModel? = null,
    val isCurrentlyInDetailScreen: Boolean = false
)