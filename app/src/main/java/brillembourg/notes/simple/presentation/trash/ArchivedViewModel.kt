package brillembourg.notes.simple.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val unArchiveNotesUseCase: UnArchiveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager
) : ViewModel() {

    private val _archivedUiState = MutableStateFlow(ArchivedUiState())
    val trashUiState = _archivedUiState.asStateFlow()

    init {
        getPreferences()
        getArchivedTasksAndObserve()
    }

    private fun getPreferences() {
        viewModelScope.launch {
            getUserPrefUseCase(GetUserPrefUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _archivedUiState.update { it.copy(noteLayout = result.data.preferences.notesLayout) }
                        }
                        is Resource.Error -> showErrorMessage(result.exception)
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun getArchivedTasksAndObserve() {
        viewModelScope.launch {
            getArchivedNotesUseCase(GetArchivedNotesUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _archivedUiState.update { uiState ->
                                uiState.copy(
                                    taskList = result.data.noteList.map { task ->
                                        task.toPresentation(dateProvider)
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
                }
        }
    }

    fun onNoteClick(it: TaskPresentationModel) {
        navigateToDetail(it)
    }

    private fun navigateToDetail(taskClicked: TaskPresentationModel) {
        _archivedUiState.update {
            it.copy(
                navigateToEditNote = ArchivedUiState.NavigateToEditNote(
                    mustConsume = true,
                    taskIndex = trashUiState.value.taskList.indexOf(taskClicked),
                    taskPresentationModel = taskClicked
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

    private fun getSelectedTasks() = _archivedUiState.value.taskList.filter { it.isSelected }

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