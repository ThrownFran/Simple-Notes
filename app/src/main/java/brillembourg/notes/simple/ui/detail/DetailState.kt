package brillembourg.notes.simple.ui.detail

import brillembourg.notes.simple.ui.models.TaskPresentationModel

sealed class DetailState {
    object CreateTask : DetailState()
    data class TaskLoaded(val task: TaskPresentationModel): DetailState()
    data class TaskSaved(val message: String) : DetailState()
    object ExitWithoutSaving: DetailState()
}