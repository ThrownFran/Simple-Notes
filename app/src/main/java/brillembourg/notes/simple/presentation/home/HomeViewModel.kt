package brillembourg.notes.simple.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toCopyString
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.presentation.trash.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getNotesUseCase: GetNotesUseCase,
    private val archiveNotesUseCase: ArchiveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val reorderNotesUseCase: ReorderNotesUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager,
) : ViewModel() {

    private val uiStateKey = "home_ui_state"

    private val _homeUiState = MutableStateFlow(getSavedUiState() ?: HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    private fun getSavedUiState(): HomeUiState? = savedStateHandle.get<HomeUiState>(uiStateKey)

    init {
        getPreferences()
        observeTaskList()
        saveChangesInSavedStateObserver()
    }

    private fun saveChangesInSavedStateObserver() {
        viewModelScope.launch {
            homeUiState.collect {
                savedStateHandle[uiStateKey] = it
            }
        }
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
        getNotesUseCase(GetNotesUseCase.Params())
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _homeUiState.update { uiState ->
                            uiState.copy(
                                noteList = NoteList(
                                    notes = result.data.noteList
                                        .map { note ->
                                            note.toPresentation(dateProvider).apply {
                                                this.isSelected =
                                                    isNoteSelectedInUi(uiState, note)
                                            }
                                        }
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
            }.launchIn(viewModelScope)
    }

    private fun isNoteSelectedInUi(
        uiState: HomeUiState,
        note: Note
    ) = (uiState.noteList.notes
        .firstOrNull() { note.id == it.id }?.isSelected
        ?: false)

    fun onAddNoteClick(content: String? = null) {
        _homeUiState.update {
            it.copy(
                navigateToAddNote = NavigateToAddNote(content),
                selectionModeActive = null
            )
        }
    }

    fun onNavigateToAddNoteCompleted() {
        _homeUiState.update { it.copy(navigateToAddNote = null) }
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

    fun onDeleteNotes() {
        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        deleteNotes(tasksToDeleteIds)

        _homeUiState.update {
            it.copy(
                showDeleteNotesConfirmation = null,
                selectionModeActive = null
            )
        }
    }

    private fun deleteNotes(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = DeleteNotesUseCase.Params(tasksToDeleteIds)
            when (val result = deleteNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
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

    fun onArchiveConfirmNotes() {

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

    fun onDismissConfirmDeleteShown() {
        _homeUiState.update { it.copy(showDeleteNotesConfirmation = null) }
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

    fun onDeleteConfirm() {
        _homeUiState.update {
            it.copy(
                showDeleteNotesConfirmation = ShowDeleteNotesConfirmationState(
                    tasksToDeleteSize = getSelectedTasks().size
                )
            )
        }
    }

    fun onShare() {
        val tasksToCopy = getSelectedTasks()
        _homeUiState.update {
            it.copy(
                shareNoteAsString = tasksToCopy.toString(),
                selectionModeActive = null
            )
        }
    }

    fun onShareCompleted() {
        _homeUiState.update { it.copy(shareNoteAsString = null) }
    }

    fun onCopy() {
        val tasksToCopy = getSelectedTasks()
        _homeUiState.update {
            it.copy(
                copyToClipboard = tasksToCopy.toCopyString(),
                selectionModeActive = null
            )
        }
    }

    fun onCopiedCompleted() {
        _homeUiState.update { it.copy(copyToClipboard = null) }
    }


}


