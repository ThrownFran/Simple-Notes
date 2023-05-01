package brillembourg.notes.simple.presentation.trash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.notes.ArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.DeleteNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.GetArchivedNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.UnArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.home.NoteList
import brillembourg.notes.simple.presentation.home.delete.NoteDeletionManager
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toCopyString
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getArchivedNotesUseCase: GetArchivedNotesUseCase,
    archiveNotesUseCase: ArchiveNotesUseCase,
    unArchiveNotesUseCase: UnArchiveNotesUseCase,
    deleteNotesUseCase: DeleteNotesUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager
) : ViewModel() {

    private val uiStateKey = "archived_ui_state"

    private val _archivedUiState = MutableStateFlow(ArchivedUiState())
    val archivedUiState = _archivedUiState.asStateFlow()

    val noteList: StateFlow<NoteList> = initNoteList(getArchivedNotesUseCase)

    private fun getSavedUiState(): ArchivedUiState? =
        savedStateHandle.get<ArchivedUiState>(uiStateKey)

    val noteDeletionManager = NoteDeletionManager(
        archiveNotesUseCase = archiveNotesUseCase,
        unArchiveNotesUseCase = unArchiveNotesUseCase,
        deleteNotesUseCase = deleteNotesUseCase,
        messageManager = messageManager,
        noteList = noteList,
        coroutineScope = viewModelScope,
        onDismissSelectionMode = {
            _archivedUiState.update { it.copy(selectionModeActive = null) }
        }
    )

    init {
        getSavedUiState()?.let { _archivedUiState.value = it }
        getPreferences()
        saveChangesInSavedStateObserver()
    }

    private fun saveChangesInSavedStateObserver() {
        viewModelScope.launch {
            archivedUiState.collect {
                savedStateHandle[uiStateKey] = it
            }
        }
    }

    private fun initNoteList(getArchivedNotesUseCase: GetArchivedNotesUseCase) =
        getArchivedNotesUseCase(GetArchivedNotesUseCase.Params())
            .transform { result ->
                when (result) {
                    is Resource.Success -> {
                        emit(NoteList(
                            notes = result.data.noteList.map { task ->
                                task.toPresentation(dateProvider)
                                    .apply {
                                        isSelected =
                                            isNoteSelectedInUi(
                                                notes = result.data.noteList
                                                    .map { it.toPresentation(dateProvider) },
                                                note = this
                                            )
                                    }
                            }
                                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                .asReversed(),
                            mustRender = true
                        ))
                    }

                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NoteList()
            )

    private fun getPreferences() {
        getUserPrefUseCase(GetUserPrefUseCase.Params())
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _archivedUiState.update { it.copy(noteLayout = result.data.preferences.noteLayout) }
                    }

                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun isNoteSelectedInUi(
        notes: List<NotePresentationModel>,
        note: NotePresentationModel
    ): Boolean {
        return (notes
            .firstOrNull() { note.id == it.id }?.isSelected
            ?: false)
    }

    fun onNoteClick(it: NotePresentationModel) {
        navigateToDetail(it)
    }

    private fun navigateToDetail(taskClicked: NotePresentationModel) {
        _archivedUiState.update {
            it.copy(
                navigateToEditNote = ArchivedUiState.NavigateToEditNote(
                    mustConsume = true,
                    taskIndex = noteList.value.notes.indexOf(taskClicked),
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

    private fun getSelectedTasks() = noteList.value.notes.filter { it.isSelected }

    fun onSelection() {
        val sizeSelected = getSelectedTasks().size
        _archivedUiState.update {
            it.copy(
                selectionModeActive = SelectionModeActive(sizeSelected)
            )
        }
    }

    fun onSelectionDismissed() {
        _archivedUiState.update { it.copy(selectionModeActive = null) }
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

    fun onShare() {
        val tasksToCopy = getSelectedTasks()
        _archivedUiState.update {
            it.copy(
                shareNoteAsString = tasksToCopy.toString(),
                selectionModeActive = null
            )
        }
    }

    fun onShareCompleted() {
        _archivedUiState.update { it.copy(shareNoteAsString = null) }
    }

    fun onCopy() {
        val tasksToCopy = getSelectedTasks()
        _archivedUiState.update {
            it.copy(
                copyToClipboard = tasksToCopy.toCopyString(),
                selectionModeActive = null
            )
        }
    }

    fun onCopiedCompleted() {
        _archivedUiState.update { it.copy(copyToClipboard = null) }
    }

}