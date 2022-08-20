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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val unArchiveTasksUseCase: UnArchiveTasksUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private var _userMessage: MutableStateFlow<UiText?> = MutableStateFlow(null)
    var userMessage: StateFlow<UiText?> = _userMessage.asStateFlow()

    private val _trashUiState = MutableStateFlow(TrashUiState())
    val trashUiState = _trashUiState.asStateFlow()

    init {
        getPreferences()
        getArchivedTasksAndObserve()
    }

    private fun getPreferences() {
        viewModelScope.launch {
            getUserPrefUseCase(GetUserPrefUseCase.Params())
                .collect {
                    when (it) {
                        is Resource.Success -> _trashUiState.value =
                            _trashUiState.value.copy(noteLayout = it.data.preferences.notesLayout)
                        is Resource.Error -> showErrorMessage(it.exception)
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
                            _trashUiState.value = _trashUiState.value.copy(
                                taskList = result.data.taskList.map { it.toPresentation(dateProvider) }
                                    .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                    .asReversed()
                            )
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

    private fun navigateToDetail(it: TaskPresentationModel) {
        _trashUiState.value = trashUiState.value.copy(
            navigateToEditNote =
            NavigateToEditNote(
                mustConsume = true,
                taskIndex = trashUiState.value.taskList.indexOf(it),
                taskPresentationModel = it
            ),
            selectionModeState = SelectionModeState()
        )
    }

    fun onNavigateToDetailCompleted() {
        val navState =
            _trashUiState.value.navigateToEditNote.copy(mustConsume = false)

        _trashUiState.value = _trashUiState.value.copy(
            navigateToEditNote = navState,
        )
    }


    private fun showErrorMessage(exception: Exception) {
        showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        _userMessage.value = message
    }

    fun onMessageDismissed() {
        _userMessage.value = null
    }

    fun onUnarchiveTasks() {
        val tasksSelectedIds = getSelectedTasks().map { it.id }
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
        deleteTasks(tasksToDeleteIds)

        _trashUiState.value = _trashUiState.value.copy(
            showArchiveNotesConfirmation = ShowDeleteNotesConfirmationState(),
            selectionModeState = SelectionModeState()
        )
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
        _trashUiState.value = _trashUiState.value.copy(
            selectionModeState = SelectionModeState(
                isActive = true,
                size = sizeSelected
            )
        )
    }

    fun onSelectionDismissed() {
        _trashUiState.value = _trashUiState.value.copy(
            selectionModeState = SelectionModeState()
        )
    }

    fun onShowConfirmDeleteNotes() {
        _trashUiState.value = _trashUiState.value.copy(
            showArchiveNotesConfirmation = ShowDeleteNotesConfirmationState(
                isVisible = true,
                tasksToDeleteSize = getSelectedTasks().size
            )
        )
    }

    fun onDismissConfirmDeleteShown() {
        _trashUiState.value = _trashUiState.value.copy(
            showArchiveNotesConfirmation = ShowDeleteNotesConfirmationState()
        )
    }

    fun onLayoutChange(noteLayout: NoteLayout) {
        _trashUiState.value = _trashUiState.value.copy(noteLayout = noteLayout)
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPreferencesUseCase(SaveUserPreferencesUseCase.Params(UserPreferences(noteLayout)))
        }
    }

}