package brillembourg.notes.simple.ui.home

import brillembourg.notes.simple.domain.models.Task

sealed class HomeState {

    data class TaskListSuccess(val taskList: List<Task>): HomeState()
    data class TaskListError(val message: String): HomeState()
    object Loading: HomeState()

}