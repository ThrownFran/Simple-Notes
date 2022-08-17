package brillembourg.notes.simple.presentation.detail

import brillembourg.notes.simple.presentation.models.TaskPresentationModel

sealed class DetailState {
    object CreateTask : DetailState()
    data class TaskLoaded(val task: TaskPresentationModel): DetailState()
    data class TaskSaved(val message: String) : DetailState()
    data class TaskCreated(val message: String) : DetailState()
    object ExitWithoutSaving: DetailState()
}