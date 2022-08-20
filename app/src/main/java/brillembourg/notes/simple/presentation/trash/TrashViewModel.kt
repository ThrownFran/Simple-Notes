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
class TrashViewModel @Inject constructor(
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val unArchiveTasksUseCase: UnArchiveTasksUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager
) : ViewModel() {

    private val _trashUiState = MutableStateFlow(TrashUiState())
    val trashUiState = _trashUiState.asStateFlow()

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
                            _trashUiState.update { it.copy(noteLayout = result.data.preferences.notesLayout) }
                        }
                        is Resource.Error -> showErrorMessage(result.exception)
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun getArchivedTasksAndObserve() {
        viewModelScope.launch {
            getArchivedTasksUseCase(GetArchivedTasksUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _trashUiState.update { uiState ->
                                uiState.copy(
                                    taskList = result.data.taskList.map { task ->
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
        _trashUiState.update {
            it.copy(
                navigateToEditNote = TrashUiState.NavigateToEditNote(
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
            _trashUiState.value.navigateToEditNote.copy(mustConsume = false)

        _trashUiState.update { it.copy(navigateToEditNote = navState) }
    }


    private fun showErrorMessage(exception: Exception) {
        showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    fun onUnarchiveTasks() {
        val tasksSelectedIds = getSelectedTasks().map { it.id }

        _trashUiState.update {
            it.copy(selectionModeActive = null)
        }

        unarchiveTasks(tasksSelectedIds)
    }

    private fun unarchiveTasks(taskToUnarchiveIds: List<Long>) {
        viewModelScope.launch {
            val params = UnArchiveTasksUseCase.Params(taskToUnarchiveIds)
            when (val result = unArchiveTasksUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteNotes() {
        val tasksToDeleteIds = getSelectedTasks().map { it.id }

        _trashUiState.update {
            it.copy(
                showArchiveNotesConfirmation = null,
                selectionModeActive = null
            )
        }

        deleteTasks(tasksToDeleteIds)
    }

    private fun getSelectedTasks() = _trashUiState.value.taskList.filter { it.isSelected }

    private fun deleteTasks(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = DeleteTasksUseCase.Params(tasksToDeleteIds)
            when (val result = deleteTasksUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onSelection() {
        val sizeSelected = getSelectedTasks().size
        _trashUiState.update {
            it.copy(
                selectionModeActive = TrashUiState.SelectionModeActive(sizeSelected)
            )
        }
    }

    fun onSelectionDismissed() {
        _trashUiState.update { it.copy(selectionModeActive = null) }
    }

    fun onShowConfirmDeleteNotes() {
        _trashUiState.update {
            it.copy(
                showArchiveNotesConfirmation = TrashUiState.ShowDeleteNotesConfirmation(
                    tasksToDeleteSize = getSelectedTasks().size
                )
            )
        }
    }

    fun onDismissConfirmDeleteShown() {
        _trashUiState.update {
            it.copy(showArchiveNotesConfirmation = null)
        }
    }

    fun onLayoutChange(noteLayout: NoteLayout) {
        _trashUiState.update { it.copy(noteLayout = noteLayout) }
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPreferencesUseCase(SaveUserPreferencesUseCase.Params(UserPreferences(noteLayout)))
        }
    }

}