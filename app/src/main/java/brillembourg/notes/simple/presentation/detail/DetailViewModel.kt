package brillembourg.notes.simple.presentation.detail

import androidx.lifecycle.*
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import brillembourg.notes.simple.presentation.extras.SingleLiveEvent
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private var currentTask: TaskPresentationModel? = null
    val state: MutableLiveData<DetailState> = MutableLiveData()
    private var content: String = ""
    private var title: String? = null

    val messageEvent: LiveData<String> get() = _messageEvent
    private val _messageEvent: SingleLiveEvent<String> = SingleLiveEvent()

    init {
        currentTask = DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).task
        currentTask?.let {
            title = it.title
            content = it.content
            setupTaskToEdit(it)
        }

        if (currentTask == null) state.value = DetailState.CreateTask
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        content = s.toString()
    }

    fun onTitleChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        title = s.toString()
    }

    private fun setupTaskToEdit(taskPresentationModel: TaskPresentationModel) {
        state.value = DetailState.TaskLoaded(taskPresentationModel)
    }

    private fun createTask() {

        if (noTitleOrContent()) {
            state.value = DetailState.ExitWithoutSaving
            return
        }

        viewModelScope.launch {
            try {
                val result = createTaskUseCase.execute(
                    CreateTaskUseCase.Params(
                        content = content,
                        title = title
                    )
                )
                state.value = DetailState.TaskCreated(result.message)
            } catch (e: Exception) {
                _messageEvent.value = "Error creating task"
            }
        }
    }

    private fun noTitleOrContent() = title.isNullOrEmpty() && content.isNullOrEmpty()

    private fun saveTask() {
        if (isEditing()) {
            currentTask?.let { updateTask(it) }
            return
        }

        createTask()
    }

    private fun isEditing(): Boolean = currentTask != null

    private fun updateTask(task: TaskPresentationModel) {
        task.content = content
        task.title = title
        saveTaskUseCase.execute(SaveTaskUseCase.Params(task.toDomain(dateProvider)))
            .onEach { state.value = DetailState.TaskSaved(it.message) }
            .launchIn(viewModelScope)
    }

    fun onBackPressed() {
        if (hasNoChanges()) {
            state.value = DetailState.ExitWithoutSaving
            return
        }
        saveTask()
    }

    private fun hasNoChanges() = currentTask?.content == content && currentTask?.title == title

}