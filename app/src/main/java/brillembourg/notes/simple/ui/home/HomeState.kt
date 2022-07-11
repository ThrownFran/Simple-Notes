package brillembourg.notes.simple.ui.home

import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.ui.TaskPresentationModel

sealed class HomeState {

    data class TaskListSuccess(val taskList: List<TaskPresentationModel>): HomeState()
    data class TaskListError(val message: String): HomeState()
    object Loading: HomeState()
}