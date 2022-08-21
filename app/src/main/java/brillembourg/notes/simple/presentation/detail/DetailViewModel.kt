package brillembourg.notes.simple.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.trash.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val archiveNotesUseCase: ArchiveNotesUseCase,
    private val unArchiveNotesUseCase: UnArchiveNotesUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager
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

    fun onArchive() {
        archiveNote()
    }

    private fun archiveNote() {
        if (currentTask?.isArchived == true) throw IllegalArgumentException("Cannot archive an archived note")

        viewModelScope.launch {
            val id = currentTask?.id ?: return@launch
            val result = archiveNotesUseCase.invoke(ArchiveNotesUseCase.Params(listOf(id)))
            when (result) {
                is Resource.Success -> {
                    _uiDetailState.update { it.copy(navigateBack = true) }
                    showMessage(result.data.message)
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onUnarchive() {
        unarchiveNote()
    }

    private fun unarchiveNote() {
        if (currentTask?.isArchived == false) throw IllegalArgumentException("Cannot unarchive a non-archived note")

        viewModelScope.launch {
            val id = currentTask?.id ?: return@launch
            val result = unArchiveNotesUseCase.invoke(UnArchiveNotesUseCase.Params(listOf(id)))
            when (result) {
                is Resource.Success -> {
                    _uiDetailState.update { it.copy(navigateBack = true) }
                    showMessage(result.data.message)
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    fun onDelete() {
        deleteNote()
    }

    private fun deleteNote() {
        if (currentTask == null) throw IllegalArgumentException("Cannot delete note that is not created")
        viewModelScope.launch {
            val id = currentTask?.id ?: return@launch
            val result = deleteNotesUseCase.invoke(DeleteNotesUseCase.Params(listOf(id)))
            when (result) {
                is Resource.Success -> {
                    _uiDetailState.update { it.copy(navigateBack = true) }
                    showMessage(result.data.message)
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
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

            val result = createNoteUseCase(
                CreateNoteUseCase.Params(
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

            val params = SaveNoteUseCase.Params(task.toDomain(dateProvider))

            when (val result = saveNoteUseCase(params)) {
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