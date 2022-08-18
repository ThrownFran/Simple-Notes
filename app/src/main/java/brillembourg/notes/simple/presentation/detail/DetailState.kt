package brillembourg.notes.simple.presentation.detail

import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.util.UiText

sealed class DetailState {
    object CreateTask : DetailState()
    data class TaskLoaded(val task: TaskPresentationModel) : DetailState()
    data class TaskSaved(val message: UiText) : DetailState()
    data class TaskCreated(val message: UiText) : DetailState()
    object ExitWithoutSaving : DetailState()
}