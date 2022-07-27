package brillembourg.notes.simple.ui.home

sealed class HomeState {

//    data class TaskListSuccess(val taskList: List<TaskPresentationModel>): HomeState()
data class ShowError(val message: String) : HomeState()
    object Loading: HomeState()
}