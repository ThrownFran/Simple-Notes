package brillembourg.notes.simple.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.DeleteTasksUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import brillembourg.notes.simple.domain.use_cases.UnArchiveTasksUseCase
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val archiveTasksUseCase: UnArchiveTasksUseCase,
    private val unArchiveTasksUseCase: UnArchiveTasksUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private var currentTask: TaskPresentationModel? = null

    private val _uiDetailState = MutableStateFlow(DetailUiState())
    val uiDetailUiState = _uiDetailState.asStateFlow()

    init {
        currentTask =
            DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).task?.copy() //copy to avoid reference in home

        currentTask?.let {
            editTaskState(it)
        }

        if (currentTask == null) {
            newTaskState()
        }
    }

    private fun newTaskState() {
        _uiDetailState.value = _uiDetailState.value.copy(
            isNewTask = true,
            focusInput = true
        )
    }

    private fun editTaskState(it: TaskPresentationModel) {
        _uiDetailState.value = _uiDetailState.value.copy(
            userInput = UserInput(it.title, it.content),
            unFocusInput = true,
            isNewTask = false,
            isArchivedTask = it.isArchived
        )
    }

    fun onBackPressed() {
        if (hasNoChangesWithOriginalTask()) {
            _uiDetailState.value = _uiDetailState.value.copy(navigateBack = true)
            return
        }
        saveTask()
    }

    fun onFocusCompleted() {
        _uiDetailState.value = _uiDetailState.value.copy(focusInput = false)
    }

    fun onUnFocusCompleted() {
        _uiDetailState.value = _uiDetailState.value.copy(unFocusInput = false)
    }

    fun onMessageShown() {
        _uiDetailState.value = _uiDetailState.value.copy(userMessage = null)
    }

    @OptIn(FlowPreview::class)
    fun onContentChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        viewModelScope.launch {
            flow { emit(s.toString()) }
                .debounce(100)
                .collect {
                    _uiDetailState.value.userInput =
                        _uiDetailState.value.userInput.copy(content = it)
                }
        }
    }

    @OptIn(FlowPreview::class)
    fun onTitleChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        viewModelScope.launch {
            flow { emit(s.toString()) }
                .debounce(100)
                .collect {
                    _uiDetailState.value.userInput =
                        _uiDetailState.value.userInput.copy(title = it)
                }
        }
    }

    private fun createTask() {

        val userInputState = _uiDetailState.value.userInput

        if (userInputState.isNullOrEmpty()) {
            _uiDetailState.value = _uiDetailState.value.copy(navigateBack = true)
            return
        }

        viewModelScope.launch {

            val result = createTaskUseCase(
                CreateTaskUseCase.Params(
                    content = userInputState.content ?: "",
                    title = userInputState.title
                )
            )

            when (result) {
                is Resource.Success -> {
                    _uiDetailState.value = _uiDetailState.value.copy(
                        userMessage = result.data.message,
                        navigateBack = true
                    )
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }

        }
    }

    private fun showErrorMessage(e: Exception) {
        _uiDetailState.value = _uiDetailState.value.copy(userMessage = getMessageFromError(e))
    }

    private fun saveTask() {
        if (isEditing()) {
            currentTask?.let { updateTask(it) }
            return
        }

        createTask()
    }

    private fun isEditing(): Boolean = currentTask != null

    private fun updateTask(task: TaskPresentationModel) {
        task.content = _uiDetailState.value.userInput.content ?: ""
        task.title = _uiDetailState.value.userInput.title

        viewModelScope.launch {

            val params = SaveTaskUseCase.Params(task.toDomain(dateProvider))

            when (val result = saveTaskUseCase(params)) {
                is Resource.Success -> {
                    _uiDetailState.value = _uiDetailState.value.copy(
                        userMessage = result.data.message,
                        navigateBack = true
                    )
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    private fun hasNoChangesWithOriginalTask() =
        currentTask?.content == _uiDetailState.value.userInput.content
                && currentTask?.title == _uiDetailState.value.userInput.title


}