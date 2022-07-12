package brillembourg.notes.simple.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import brillembourg.notes.simple.ui.TaskPresentationModel
import brillembourg.notes.simple.ui.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase
) : ViewModel() {

    private var currentTask: TaskPresentationModel? = null
    val state: MutableLiveData<DetailState> = MutableLiveData()
    private var content: String = ""

    init {
        currentTask = DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).task
        currentTask?.let {
            content = it.content
            setupTaskToEdit(it)
        }
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        content = s.toString()
    }

    private fun setupTaskToEdit(taskPresentationModel: TaskPresentationModel) {
        state.value = DetailState.TaskLoaded(taskPresentationModel)
    }

    private fun createTask() {
        createTaskUseCase.execute(CreateTaskUseCase.Params(content))
            .onEach { state.value = DetailState.TaskSaved(it.message) }
            .launchIn(viewModelScope)
    }

    private fun saveTask() {
        currentTask?.let { task ->
            updateTask(task)
            return
        }

        createTask()
    }

    private fun updateTask(task: TaskPresentationModel) {
        task.content = content
        saveTaskUseCase.execute(SaveTaskUseCase.Params(task.toDomain()))
            .onEach { state.value = DetailState.TaskSaved(it.message) }
            .launchIn(viewModelScope)
    }

    fun onBackPressed() {
        if (currentTask?.content == content) {
            state.value = DetailState.ExitWithoutSaving
            return
        }
        saveTask()
    }

}