package brillembourg.notes.simple.ui.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import brillembourg.notes.simple.ui.TaskPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val saveTaskUseCase: SaveTaskUseCase
) : ViewModel() {

    private var task : TaskPresentationModel
    val state: MutableLiveData<DetailState> = MutableLiveData()

    init {
        task = DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).task?:createTask()
        editTask(task)
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        task.content = s.toString()
    }

    private fun editTask(taskPresentationModel: TaskPresentationModel) {
        state.value = DetailState.TaskLoaded(taskPresentationModel)
    }

    private fun createTask(): TaskPresentationModel {
        //TODO
        return TaskPresentationModel(2L,"","")
    }

    fun saveTask() {
        saveTaskUseCase.execute(SaveTaskUseCase.Params(task.id,task.content))
            .onEach { state.value = DetailState.TaskSaved(it.message) }
            .launchIn(viewModelScope)
    }

}