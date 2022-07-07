package brillembourg.notes.simple.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import brillembourg.notes.simple.ui.TaskPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveTaskUseCase: SaveTaskUseCase
) : ViewModel() {

    private var currentTask : TaskPresentationModel
    private var originalTask : TaskPresentationModel
    val state: MutableLiveData<DetailState> = MutableLiveData()

    init {
        currentTask = DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).task?:createTask()
        originalTask = currentTask.copy()
        editTask(currentTask)
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        currentTask.content = s.toString()
    }

    private fun editTask(taskPresentationModel: TaskPresentationModel) {
        state.value = DetailState.TaskLoaded(taskPresentationModel)
    }

    private fun createTask(): TaskPresentationModel {
        return TaskPresentationModel(-1L,"","")
    }

    fun saveTask() {
        saveTaskUseCase.execute(SaveTaskUseCase.Params(currentTask.id,currentTask.content))
            .onEach { state.value = DetailState.TaskSaved(it.message) }
            .launchIn(viewModelScope)
    }

    fun onBackPressed() {
        if(currentTask == originalTask) {
            state.value = DetailState.ExitWithoutSaving
            return
        }
        saveTask()
    }

}