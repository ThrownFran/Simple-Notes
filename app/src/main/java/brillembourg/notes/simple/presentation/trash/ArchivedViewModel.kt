package brillembourg.notes.simple.presentation.trash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val unArchiveNotesUseCase: UnArchiveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager
) : ViewModel() {

    private val uiStateKey = "archived_ui_state"

    private val _archivedUiState = MutableStateFlow(getSavedUiState() ?: ArchivedUiState())
    val archivedUiState = _archivedUiState.asStateFlow()

    private fun getSavedUiState(): ArchivedUiState? =
        savedStateHandle.get<ArchivedUiState>(uiStateKey)

    init {
        getPreferences()
        getArchivedTasksAndObserve()
        saveChangesInSavedStateObserver()
    }

    private fun saveChangesInSavedStateObserver() {
        viewModelScope.launch {
            archivedUiState.collect {
                savedStateHandle[uiStateKey] = it
            }
        }
    }

    private fun getPreferences() {
        getUserPrefUseCase(GetUserPrefUseCase.Params())
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _archivedUiState.update { it.copy(noteLayout = result.data.preferences.notesLayout) }
                    }
                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun getArchivedTasksAndObserve() {
        getArchivedNotesUseCase(GetArchivedNotesUseCase.Params())
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _archivedUiState.update { uiState ->
                            uiState.copy(
                                noteList = result.data.noteList.map { task ->
                                    task.toPresentation(dateProvider)
                                        .apply {
                                            this.isSelected = isNoteSelectedInUi(uiState, this)
                                        }
                                }
                                    .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                    .asReversed()
                            )
                        }
                    }
                    is Resource.Error ->
                        showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun isNoteSelectedInUi(
        uiState: ArchivedUiState,
        note: NotePresentationModel
    ) = (uiState.noteList
        .firstOrNull() { note.id == it.id }?.isSelected
        ?: false)

    fun onNoteClick(it: NotePresentationModel) {
        navigateToDetail(it)
    }

    private fun navigateToDetail(taskClicked: NotePresentationModel) {
        _archivedUiState.update {
            it.copy(
                navigateToEditNote = ArchivedUiState.NavigateToEditNote(
                    mustConsume = true,
                    taskIndex = archivedUiState.value.noteList.indexOf(taskClicked),
                    notePresentationModel = taskClicked
                ),
                selectionModeActive = null
            )
        }
    }

    fun onNavigateToDetailCompleted() {
        val navState =
            _archivedUiState.value.navigateToEditNote.copy(mustConsume = false)

        _archivedUiState.update { it.copy(navigateToEditNote = navState) }
    }


    private fun showErrorMessage(exception: Exception) {
        showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    fun onUnarchiveTasks() {
        val tasksSelectedIds = getSelectedTasks().map { it.id }

        _archivedUiState.update {
            it.copy(selectionModeActive = null)
        }

        unarchiveTasks(tasksSelectedIds)
    }

    private fun unarchiveTasks(taskToUnarchiveIds: List<Long>) {
        viewModelScope.launch {
            val params = UnArchiveNotesUseCase.Params(taskToUnarchiveIds)
            when (val result = unArchiveNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteNotes() {
        val tasksToDeleteIds = getSelectedTasks().map { it.id }

        _archivedUiState.update {
            it.copy(
                showArchiveNotesConfirmation = null,
                selectionModeActive = null
            )
        }

        deleteTasks(tasksToDeleteIds)
    }

    private fun getSelectedTasks() = _archivedUiState.value.noteList.filter { it.isSelected }

    private fun deleteTasks(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = DeleteNotesUseCase.Params(tasksToDeleteIds)
            when (val result = deleteNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onSelection() {
        val sizeSelected = getSelectedTasks().size
        _archivedUiState.update {
            it.copy(
                selectionModeActive = ArchivedUiState.SelectionModeActive(sizeSelected)
            )
        }
    }

    fun onSelectionDismissed() {
        _archivedUiState.update { it.copy(selectionModeActive = null) }
    }

    fun onShowConfirmDeleteNotes() {
        _archivedUiState.update {
            it.copy(
                showArchiveNotesConfirmation = ArchivedUiState.ShowDeleteNotesConfirmation(
                    tasksToDeleteSize = getSelectedTasks().size
                )
            )
        }
    }

    fun onDismissConfirmDeleteShown() {
        _archivedUiState.update {
            it.copy(showArchiveNotesConfirmation = null)
        }
    }

    fun onLayoutChange(noteLayout: NoteLayout) {
        _archivedUiState.update { it.copy(noteLayout = noteLayout) }
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPrefUseCase(SaveUserPrefUseCase.Params(UserPreferences(noteLayout)))
        }
    }

}