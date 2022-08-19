package brillembourg.notes.simple.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTaskListUseCase: GetTaskListUseCase,
    private val archiveTasksUseCase: ArchiveTasksUseCase,
    private val reorderTaskListUseCase: ReorderTaskListUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    //Task list state separated from uiState to improve performance
    private val _taskListState: MutableStateFlow<List<TaskPresentationModel>> =
        MutableStateFlow(ArrayList())
    var taskListState = _taskListState.asStateFlow()

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    init {
        getPreferences()
        observeTaskList()
    }

    private fun getPreferences() {
        viewModelScope.launch {
            getUserPrefUseCase(GetUserPrefUseCase.Params())
                .collect {
                    when (it) {
                        is Resource.Success -> _homeUiState.value =
                            _homeUiState.value.copy(noteLayout = it.data.preferences.notesLayout)
                        is Resource.Error -> showErrorMessage(it.exception)
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun observeTaskList() {
        viewModelScope.launch {
            getTaskListUseCase(GetTaskListUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _taskListState.value = result.data.taskList
                                .map { it.toPresentation(dateProvider) }
                                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                .asReversed()
                        }
                        is Resource.Error -> {
                            showMessage(UiText.DynamicString("Error loading tasks"))
                        }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    fun onAddNoteClick() {
        _homeUiState.value = _homeUiState.value.copy(
            navigateToAddNote = true,
            selectionModeState = SelectionModeState()
        )
    }

    fun onNavigateToAddNoteCompleted() {
        _homeUiState.value = _homeUiState.value.copy(navigateToAddNote = false)
    }

    fun onReorderedNotes(reorderedTaskList: List<TaskPresentationModel>) {
        _homeUiState.value = _homeUiState.value.copy(
            selectionModeState = SelectionModeState()
        )

        if (reorderedTaskList == taskListState.value) return
        reorderTasks(reorderedTaskList)
    }

    private fun reorderTasks(reorderedTaskList: List<TaskPresentationModel>) {
        viewModelScope.launch {
            val params = ReorderTaskListUseCase.Params(reorderedTaskList.map {
                it.toDomain(dateProvider)
            })

            when (val result = reorderTaskListUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }


    fun onNoteClick(it: TaskPresentationModel) {
        navigateToDetail(it)
    }

    private fun navigateToDetail(it: TaskPresentationModel) {
        _homeUiState.value = _homeUiState.value.copy(
            navigateToEditNote = NavigateToEditNote(
                mustConsume = true,
                taskIndex = taskListState.value.indexOf(it),
                taskPresentationModel = it
            ),
            selectionModeState = SelectionModeState()
        )
    }

    fun onNavigateToDetailCompleted() {

        val navState =
            _homeUiState.value.navigateToEditNote.copy(mustConsume = false)

        _homeUiState.value = _homeUiState.value.copy(
            navigateToEditNote = navState,
        )
    }

    private fun showErrorMessage(exception: Exception) {
        showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        _homeUiState.value = homeUiState.value.copy(userMessage = message)
    }

    fun onMessageShown() {
        _homeUiState.value = _homeUiState.value.copy(userMessage = null)
    }

    fun onArchiveNotes() {

        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        archiveNotes(tasksToDeleteIds)

        _homeUiState.value = _homeUiState.value.copy(
            showArchiveNotesConfirmation = ShowArchiveNotesConfirmationState(),
            selectionModeState = SelectionModeState()
        )

    }

    private fun archiveNotes(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = ArchiveTasksUseCase.Params(tasksToDeleteIds)
            when (val result = archiveTasksUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }

        }
    }

    fun onSelection() {
        val sizeSelected = getSelectedTasks().size
        _homeUiState.value = _homeUiState.value.copy(
            selectionModeState = SelectionModeState(
                isActive = true, size = sizeSelected
            )
        )
    }

    fun onSelectionDismissed() {
        _homeUiState.value = _homeUiState.value.copy(
            selectionModeState = SelectionModeState()
        )
    }

    private fun getSelectedTasks() = _taskListState.value.filter { it.isSelected }

    fun onShowConfirmArchiveNotes() {
        _homeUiState.value = _homeUiState.value.copy(
            showArchiveNotesConfirmation = ShowArchiveNotesConfirmationState(
                isVisible = true,
                tasksToArchiveSize = getSelectedTasks().size
            )
        )
    }

    fun onDismissConfirmArchiveShown() {
        _homeUiState.value = _homeUiState.value.copy(
            showArchiveNotesConfirmation = ShowArchiveNotesConfirmationState()
        )
    }

    fun onLayoutChange(noteLayout: NoteLayout) {
        _homeUiState.value = _homeUiState.value.copy(noteLayout = noteLayout)
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPreferencesUseCase(SaveUserPreferencesUseCase.Params(UserPreferences(noteLayout)))
        }
    }


}