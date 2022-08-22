package brillembourg.notes.simple.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val archiveNotesUseCase: ArchiveNotesUseCase,
    private val reorderNotesUseCase: ReorderNotesUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    init {
        getPreferences()
        observeTaskList()
    }

    private fun getPreferences() {
        viewModelScope.launch {
            getUserPrefUseCase(GetUserPrefUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _homeUiState.update { it.copy(noteLayout = result.data.preferences.notesLayout) }
                        }
                        is Resource.Error -> showErrorMessage(result.exception)
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun observeTaskList() {
        viewModelScope.launch {
            getNotesUseCase(GetNotesUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {

                            _homeUiState.update {
                                it.copy(
                                    noteList = NoteList(
                                        notes = result.data.noteList
                                            .map { it.toPresentation(dateProvider) }
                                            .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                            .asReversed(),
                                        mustRender = true)
                                )
                            }

                        }
                        is Resource.Error -> {
                            showErrorMessage(result.exception)
                        }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    fun onAddNoteClick() {
        _homeUiState.update {
            it.copy(
                navigateToAddNote = true,
                selectionModeActive = null
            )
        }
    }

    fun onNavigateToAddNoteCompleted() {
        _homeUiState.update { it.copy(navigateToAddNote = false) }
    }

    fun onReorderedNotes(reorderedTaskList: List<NotePresentationModel>) {
        _homeUiState.update {

            val noteList: List<NotePresentationModel> = _homeUiState.value.noteList.notes
            noteList.forEach { it.isSelected = false }

            it.copy(
                selectionModeActive = null,
                noteList = _homeUiState.value.noteList.copy(
                    notes = noteList,
                    mustRender = false
                )
            )
        }

        if (reorderedTaskList == _homeUiState.value.noteList.notes) return
        reorderTasks(reorderedTaskList)
    }

    private fun reorderTasks(reorderedTaskList: List<NotePresentationModel>) {
        viewModelScope.launch {
            val params = ReorderNotesUseCase.Params(reorderedTaskList.map {
                it.toDomain(dateProvider)
            })

            when (val result = reorderNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }


    fun onNoteClick(it: NotePresentationModel) {
        navigateToDetail(it)
    }

    private fun navigateToDetail(note: NotePresentationModel) {
        _homeUiState.update {
            it.copy(
                navigateToEditNote = NavigateToEditNote(
                    mustConsume = true,
                    taskIndex = _homeUiState.value.noteList.notes.indexOf(note),
                    notePresentationModel = note
                ),
                selectionModeActive = null
            )
        }
    }

    fun onNavigateToDetailCompleted() {

        val navState =
            _homeUiState.value.navigateToEditNote.copy(mustConsume = false)

        _homeUiState.update {
            it.copy(
                navigateToEditNote = navState
            )
        }
    }

    private fun showErrorMessage(exception: Exception) {
        messageManager.showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    fun onArchiveNotes() {

        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        archiveNotes(tasksToDeleteIds)

        _homeUiState.update {
            it.copy(
                showArchiveNotesConfirmation = null,
                selectionModeActive = null
            )
        }

    }

    private fun archiveNotes(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = ArchiveNotesUseCase.Params(tasksToDeleteIds)
            when (val result = archiveNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }

        }
    }

    fun onSelection() {
        val sizeSelected = getSelectedTasks().size

        _homeUiState.update {
            it.copy(
                selectionModeActive = SelectionModeActive(
                    size = sizeSelected
                )
            )
        }

    }

    fun onSelectionDismissed() {
        _homeUiState.update { it.copy(selectionModeActive = null) }
    }

    private fun getSelectedTasks() = _homeUiState.value.noteList.notes.filter { it.isSelected }

    fun onShowConfirmArchiveNotes() {

        _homeUiState.update {
            it.copy(
                showArchiveNotesConfirmation = ShowArchiveNotesConfirmationState(
                    tasksToArchiveSize = getSelectedTasks().size
                )
            )
        }
    }

    fun onDismissConfirmArchiveShown() {
        _homeUiState.update { it.copy(showArchiveNotesConfirmation = null) }
    }

    fun onLayoutChange(noteLayout: NoteLayout) {
        _homeUiState.update { it.copy(noteLayout = noteLayout) }
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPrefUseCase(SaveUserPrefUseCase.Params(UserPreferences(noteLayout)))
        }
    }


}