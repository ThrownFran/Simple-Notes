package brillembourg.notes.simple.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.presentation.trash.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
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

    private var currentTask: NotePresentationModel? = null
    var messageToShowWhenNavBack: UiText? = null

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

        onInputChangeFlow()
    }

    /*UserInput two way data binding*/
    private fun onInputChangeFlow() {
        viewModelScope.launch {
            _uiDetailState.value.getOnInputChangedFlow()
                .debounce(300)
                .collect {
                    saveTask(navigateBack = false)
                }
        }
    }

    private fun newTaskState() {
        _uiDetailState.update {
            it.copy(
                isNewTask = true,
                focusInput = true,
            )
        }
    }

    private fun editTaskState(note: NotePresentationModel) {
        _uiDetailState.update {
            it.copy(
                userInput = _uiDetailState.value.userInput.copy(
                    title = note.title ?: "",
                    content = note.content,
//                    render = true
                ),
                unFocusInput = true,
                isNewTask = false,
                isArchivedTask = note.isArchived
            )
        }
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
                    showMessage(result.data.message)
                    _uiDetailState.update { it.copy(navigateBack = true) }
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
                    showMessage(result.data.message)
                    _uiDetailState.update { it.copy(navigateBack = true) }
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onBackPressed() {
        if (hasNoChangesWithOriginalTask()) {
            messageToShowWhenNavBack?.let { showMessage(it) }
            _uiDetailState.update { it.copy(navigateBack = true) }
            return
        }
        saveTask(navigateBack = true)
    }

    fun onFocusCompleted() {
        _uiDetailState.update { it.copy(focusInput = false) }
    }

    fun onUnFocusCompleted() {
        _uiDetailState.update { it.copy(unFocusInput = false) }
    }

    private fun createTask(
        navigateBack: Boolean = true,
    ) {

        val userInputState = _uiDetailState.value.userInput

        if (userInputState.isNullOrEmpty()) {
            _uiDetailState.update { it.copy(navigateBack = true) }
            return
        }

        viewModelScope.launch {

            val result = createNoteUseCase(
                CreateNoteUseCase.Params(
                    content = userInputState.content,
                    title = userInputState.title
                )
            )

            when (result) {
                is Resource.Success -> {
                    messageToShowWhenNavBack = result.data.message
                    _uiDetailState.update {
                        it.copy(
                            navigateBack = navigateBack,
                        )
                    }
                    currentTask = result.data.note.toPresentation(dateProvider)
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }

        }
    }

    private fun showErrorMessage(e: Exception) {
        messageManager.showMessage(getMessageFromError(e))
    }

    private fun saveTask(
        navigateBack: Boolean = true,
    ) {
        if (currentTask != null) {
            currentTask?.let { updateTask(it, navigateBack) }
            return
        }

        createTask(navigateBack)
    }

    private fun updateTask(
        task: NotePresentationModel,
        navigateBack: Boolean = true
    ) {

        task.content = _uiDetailState.value.userInput.content
        task.title = _uiDetailState.value.userInput.title

        viewModelScope.launch {

            val params = SaveNoteUseCase.Params(task.toDomain(dateProvider))

            when (val result = saveNoteUseCase(params)) {
                is Resource.Success -> {
                    messageToShowWhenNavBack = result.data.message
                    _uiDetailState.update {
                        it.copy(
                            navigateBack = navigateBack,
                        )
                    }
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