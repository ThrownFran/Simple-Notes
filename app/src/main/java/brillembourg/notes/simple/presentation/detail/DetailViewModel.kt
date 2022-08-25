package brillembourg.notes.simple.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val archiveNotesUseCase: ArchiveNotesUseCase,
    private val unArchiveNotesUseCase: UnArchiveNotesUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager,
) : ViewModel() {

    private val uiStateKey = "detail_ui_state"
    private val noteSavedStateKey = "note_to_edit"

    private var currentNotePresentation: NotePresentationModel? =
        getSavedTaskFromDeath()?.copy() //copy to avoid reference in home
            ?: getSavedNoteFromNav()?.copy() //Argument from navigation

    private val _uiDetailState =
        MutableStateFlow(getSavedUiStateFromDeath() ?: DetailUiState())
            .apply {
                observeInputChanges(value)
                onEach { saveStateInSavedStateHandler(value) }
                    .launchIn(viewModelScope)
            }


    val uiDetailUiState = _uiDetailState.asStateFlow()

    private fun getSavedUiStateFromDeath(): DetailUiState? =
        savedStateHandle.get<DetailUiState>(uiStateKey)

    private fun getSavedTaskFromDeath(): NotePresentationModel? =
        savedStateHandle.get<NotePresentationModel>(noteSavedStateKey)

    private fun getSavedNoteFromNav() =
        DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).task

    var messageToShowWhenNavBack: UiText? = null

    init {
        currentNotePresentation?.let {
            editTaskState(it)
        }

        if (currentNotePresentation == null) {
            newTaskState()
        }

//        observeInputChanges()
        saveStateInSavedStateHandler()
    }

    private fun observeInputChanges(uiState: DetailUiState) {
        uiState.getOnInputChangedFlow()
            .debounce(200)
            .onEach {
                onInputChange()
            }.launchIn(viewModelScope)
    }

    private fun onInputChange() {
        saveTask(navigateBack = false)
        //Save new input in state
        saveStateInSavedStateHandler(_uiDetailState.value)
    }

    private fun newTaskState() {
        val contentOptional: String? =
            DetailFragmentArgs.fromSavedStateHandle(savedStateHandle).contentOptional
        val currentContent: String = _uiDetailState.value.userInput.content

        val content: String = when {
            currentContent.isNotEmpty() -> currentContent //Entry from death restore
            contentOptional?.isNotEmpty() == true -> contentOptional //Entry with incoming Content
            else -> "" //Entry from Home
        }

        _uiDetailState.update {
            it.copy(
                userInput = it.userInput.copy(content = content),
                isNewTask = true,
                focusInput = contentOptional.isNullOrEmpty(),
            )
        }
    }

    private fun editTaskState(note: NotePresentationModel) {
        _uiDetailState.update {
            it.copy(
                userInput = _uiDetailState.value.userInput.copy(
                    title = note.title ?: "",
                    content = note.content,
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
        if (currentNotePresentation?.isArchived == true) throw IllegalArgumentException("Cannot archive an archived note")

        viewModelScope.launch {
            val id = currentNotePresentation?.id ?: return@launch
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
        if (currentNotePresentation?.isArchived == false) throw IllegalArgumentException("Cannot unarchive a non-archived note")

        viewModelScope.launch {
            val id = currentNotePresentation?.id ?: return@launch
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
        if (currentNotePresentation == null) throw IllegalArgumentException("Cannot delete note that is not created")
        viewModelScope.launch {
            val id = currentNotePresentation?.id ?: return@launch
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

                    if (navigateBack) showMessage(result.data.message) else messageToShowWhenNavBack =
                        result.data.message

                    _uiDetailState.update {
                        it.copy(navigateBack = navigateBack)
                    }
                    currentNotePresentation = result.data.note.toPresentation(dateProvider)
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
        if (currentNotePresentation != null) {
            currentNotePresentation?.let { updateTask(it, navigateBack) }
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
                    if (navigateBack) {
                        showMessage(result.data.message)
                    } else {
                        messageToShowWhenNavBack = result.data.message
                    }
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
        currentNotePresentation?.content == _uiDetailState.value.userInput.content
                && currentNotePresentation?.title == _uiDetailState.value.userInput.title

//    private fun saveChangesInSavedStateObserver() {
//        viewModelScope.launch {
//            uiDetailUiState.collect {
//                saveStateInSavedStateHandler()
//            }
//        }
//    }

    private fun saveStateInSavedStateHandler(detailUiState: DetailUiState) {
        savedStateHandle[uiStateKey] = detailUiState
        savedStateHandle[noteSavedStateKey] = currentNotePresentation
    }

    private fun saveStateInSavedStateHandler() {
        savedStateHandle[uiStateKey] = _uiDetailState.value
        savedStateHandle[noteSavedStateKey] = currentNotePresentation
    }


}