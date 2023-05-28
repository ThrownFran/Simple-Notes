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
import brillembourg.notes.simple.presentation.home.stopTimeoutMillis
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toCopyString
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
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

    private val key: MutableStateFlow<String> = MutableStateFlow("")

    private val noteList: StateFlow<NoteList> = initNoteList(getArchivedNotesUseCase)

    private val noteLayout: StateFlow<NoteLayout> = initPreferences()
    private val selectionModeActive: MutableStateFlow<SelectionModeActive> =
        MutableStateFlow(getSavedUiState()?.selectionModeActive ?: SelectionModeActive())
    private val noteActions: MutableStateFlow<ArchivedUiState.NoteActions> =
        MutableStateFlow(
            getSavedUiState()?.noteActions ?: ArchivedUiState.NoteActions()
        )

    private val _navigates: MutableStateFlow<ArchivedUiNavigates> =
        MutableStateFlow(ArchivedUiNavigates.Idle)
    val navigates = _navigates.asStateFlow()

    private val isWizardVisible: StateFlow<EmptyNote> = combine(noteList, key) { noteList, key ->
        when {
            key.isEmpty() && noteList.notes.isEmpty() -> EmptyNote.NoArchived
            key.isNotEmpty() && noteList.notes.isEmpty() -> EmptyNote.EmptyForSearch
            else -> EmptyNote.None
        }
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = EmptyNote.None
        )

    private fun getSavedUiState(): ArchivedUiState? =
        savedStateHandle.get<ArchivedUiState>(uiStateKey)

    val archivedUiState: StateFlow<ArchivedUiState> = combine(
        flow = noteList,
        flow2 = noteLayout,
        flow3 = isWizardVisible,
        flow4 = selectionModeActive,
        flow5 = noteActions
    ) { noteList, noteLayout, isWizardVisible, selectionModeActive, noteActions ->
        ArchivedUiState(
            noteList = noteList,
            noteLayout = noteLayout,
            selectionModeActive = selectionModeActive,
            noteActions = noteActions,
            emptyNote = isWizardVisible
        )
    }.distinctUntilChanged()
        .onEach {
            savedStateHandle[uiStateKey] = it
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = getSavedUiState() ?: ArchivedUiState()
        )

    val noteDeletionManager = NoteDeletionManager(
        archiveNotesUseCase = archiveNotesUseCase,
        unArchiveNotesUseCase = unArchiveNotesUseCase,
        deleteNotesUseCase = deleteNotesUseCase,
        messageManager = messageManager,
        noteList = noteList,
        coroutineScope = viewModelScope,
        onDismissSelectionMode = {
            selectionModeActive.update { SelectionModeActive() }
        }
    )

    private fun initNoteList(getArchivedNotesUseCase: GetArchivedNotesUseCase) =
        key
            .debounce(100)
            .distinctUntilChanged()
            .map { key ->
                GetArchivedNotesUseCase.Params(key)
            }
            .flatMapLatest { params ->
                getArchivedNotesUseCase(params)
            }
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
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
                initialValue = NoteList()
            )

    private fun initPreferences() = getUserPrefUseCase(GetUserPrefUseCase.Params())
        .transform { result ->
            when (result) {
                is Resource.Success -> emit(result.data.preferences.noteLayout)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = NoteLayout.Vertical
        )

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
        _navigates.update {
            ArchivedUiNavigates.NavigateToEditNote(
                mustConsume = true,
                taskIndex = noteList.value.notes.indexOf(taskClicked),
                notePresentationModel = taskClicked
            )
        }
        selectionModeActive.update { SelectionModeActive() }
    }

    fun onNavigateToDetailCompleted() {
        _navigates.update { ArchivedUiNavigates.Idle }
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
        selectionModeActive.update { SelectionModeActive(size = sizeSelected) }
    }

    fun onSelectionDismissed() {
        selectionModeActive.update { SelectionModeActive() }
    }

    fun onLayoutChange(noteLayout: NoteLayout) {
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPrefUseCase(SaveUserPrefUseCase.Params(UserPreferences(noteLayout)))
        }
    }

    fun onShare() {
        val tasksToCopy = getSelectedTasks()
        noteActions.update {
            ArchivedUiState.NoteActions(
                shareNoteAsString = tasksToCopy.toString()
            )
        }
    }

    fun onShareCompleted() {
        noteActions.update {
            ArchivedUiState.NoteActions()
        }
    }

    fun onCopy() {
        val tasksToCopy = getSelectedTasks()
        noteActions.update {
            ArchivedUiState.NoteActions(
                copyToClipboard = tasksToCopy.toCopyString()
            )
        }
        selectionModeActive.update { SelectionModeActive() }
    }

    fun onCopiedCompleted() {
        noteActions.update {
            ArchivedUiState.NoteActions()
        }
    }

    fun onSearch(key: String) {
        this.key.update { key }
    }

    enum class EmptyNote {
        None, EmptyForSearch, NoArchived
    }
}

