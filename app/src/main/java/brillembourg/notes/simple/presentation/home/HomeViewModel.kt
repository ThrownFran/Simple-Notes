@file:OptIn(ExperimentalCoroutinesApi::class)

package brillembourg.notes.simple.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.ArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.DeleteNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.GetNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.ReorderNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.UnArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import brillembourg.notes.simple.presentation.categories.toDomain
import brillembourg.notes.simple.presentation.categories.toPresentation
import brillembourg.notes.simple.presentation.home.delete.NoteDeletionManager
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toCopyString
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.presentation.trash.SelectionModeActive
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val stopTimeoutMillis: Long = 5_000

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getNotesUseCase: GetNotesUseCase,
    private val reorderNotesUseCase: ReorderNotesUseCase,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val dateProvider: DateProvider,
    private val messageManager: MessageManager,
    private val saveFilterByCategoriesUseCase: SaveFilterByCategoriesUseCase,
    archiveNotesUseCase: ArchiveNotesUseCase,
    unArchiveNotesUseCase: UnArchiveNotesUseCase,
    deleteNotesUseCase: DeleteNotesUseCase,
    getFilteredCategoriesUseCase: GetFilterByCategoriesUseCase,
    getAvailableCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val uiStateKey = "home_ui_state"

    private val _navigates: MutableStateFlow<HomeUiNavigates> =
        MutableStateFlow(HomeUiNavigates.Idle)
    val navigates = _navigates.asStateFlow()

    val allCategories: StateFlow<List<CategoryPresentationModel>> =
        initAllCategories(getAvailableCategoriesUseCase)
    private val filteredCategories: StateFlow<List<CategoryPresentationModel>> =
        initFilteredCategories(getFilteredCategoriesUseCase)

    private val key: MutableStateFlow<String> = MutableStateFlow("")
    private val selectionModeActive: MutableStateFlow<SelectionModeActive> =
        MutableStateFlow(getSavedUiState()?.selectionModeActive ?: SelectionModeActive())
    private val noteList: StateFlow<NoteList> = initNoteList()
    private val noteLayout: StateFlow<NoteLayout> = initPreferences()
    private val noteActions: MutableStateFlow<NoteActions> =
        MutableStateFlow(getSavedUiState()?.noteActions ?: NoteActions())
    private val selectCategoriesState: MutableStateFlow<SelectCategoriesState> =
        MutableStateFlow(SelectCategoriesState())

    val homeUiState: StateFlow<HomeUiState> = combine(
        noteList,
        noteLayout,
        selectionModeActive,
        noteActions,
        selectCategoriesState
    ) { noteList, noteLayout, selectionModeActive, noteActions, selectCategoriesState ->
        HomeUiState(
            noteList = noteList,
            noteLayout = noteLayout,
            selectionModeActive = selectionModeActive,
            noteActions = noteActions,
            selectCategoriesState = selectCategoriesState
        )
    }.distinctUntilChanged()
        .onEach {
            savedStateHandle[uiStateKey] = it
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = getSavedUiState() ?: HomeUiState()
        )

    private fun getSavedUiState() = savedStateHandle.get<HomeUiState>(uiStateKey)

    val noteDeletionManager = NoteDeletionManager(
        archiveNotesUseCase = archiveNotesUseCase,
        unArchiveNotesUseCase = unArchiveNotesUseCase,
        deleteNotesUseCase = deleteNotesUseCase,
        messageManager = messageManager,
        noteList = noteList,
        coroutineScope = viewModelScope,
        onDismissSelectionMode = {
            onSelectionEnd()
        }
    )

    private fun initNoteList() = key
        .debounce(100)
        .distinctUntilChanged()
        .combine(filteredCategories) { key, filteredCategories ->
            GetNotesUseCase.Params(
                filterByCategories = filteredCategories.map { it.toDomain() },
                keySearch = key
            )
        }
        .flatMapLatest { params ->
            getNotesUseCase(params)
        }
        .combine(selectionModeActive) { result, selectionMode ->
            Pair(result, selectionMode)
        }
        .transform { pair ->
            val result = pair.first
            val selectionMode = pair.second
            when (result) {
                is Resource.Success -> {
                    emit(
                        NoteList(
                            notes = result.data.noteList
                                .map { noteWithCategories ->
                                    noteWithCategories.toPresentation(dateProvider)
                                        .apply {
                                            this.isSelected =
                                                selectionMode.selectedIds.contains(
                                                    noteWithCategories.note.id
                                                )
                                        }
                                }
                                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                .asReversed(),
                            mustRender = true,
                            filteredCategories = filteredCategories.value,
                            key = key.value
                        ))
                }

                is Resource.Error -> showErrorMessage(result.exception)
                else -> Unit
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = NoteList()
        )

    private fun initAllCategories(getAvailableCategoriesUseCase: GetCategoriesUseCase) =
        getAvailableCategoriesUseCase.invoke(GetCategoriesUseCase.Params())
            .transform { result ->
                when (result) {
                    is Resource.Success -> {
                        emit(
                            result.data.categoryList
                                .map { it.toPresentation() }.toDiplayOrder()
                        )
                    }
                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
                initialValue = emptyList()
            )

    private fun initFilteredCategories(getFilteredCategoriesUseCase: GetFilterByCategoriesUseCase) =
        getFilteredCategoriesUseCase.invoke(GetFilterByCategoriesUseCase.Params())
            .transform { result ->
                when (result) {
                    is Resource.Success -> {
                        emit(result.data.categories
                            .map { it.toPresentation() }
                            .toDiplayOrder())
                    }

                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
                initialValue = emptyList()
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

    //region Note list

    fun onNoteClick(it: NotePresentationModel) {
        navigateToDetail(it)
    }

    private fun isNoteSelectedInUi(
        note: Note
    ): Boolean {
        return selectionModeActive.value?.selectedIds?.contains(note.id) ?: false
    }
    //endregion

    //region Categories

    fun onNavigateToCategories() {
        selectCategoriesState.update {
            it.copy(navigate = true)
        }
    }

    fun onHideCategories() {
        selectCategoriesState.update {
            it.copy(isShowing = false)
        }
    }

    fun onCategoriesShowing() {
        selectCategoriesState.update {
            it.copy(
                isShowing = true,
                navigate = false
            )
        }
    }

    fun onCategoryChecked(category: CategoryPresentationModel, isChecked: Boolean) {
        val categoryUpdated = category.copy(isSelected = isChecked)

        val currentCategories: List<CategoryPresentationModel> = filteredCategories.value

        val categoriesUpdated =
            if (isChecked) currentCategories + categoryUpdated
            else currentCategories - categoryUpdated

        viewModelScope.launch {
            val params = SaveFilterByCategoriesUseCase.Params(categoriesUpdated.map { it.id })
            saveFilterByCategoriesUseCase.invoke(params)
        }
    }

    //endregion

    //region Navigation

    fun onAddNoteClick(content: String? = null) {
        navigateToAddNote(content)
    }

    private fun navigateToAddNote(content: String?) {
        _navigates.update { HomeUiNavigates.NavigateToAddNote(content) }
        onSelectionEnd()
    }

    fun onNavigateToAddNoteCompleted() {
        _navigates.update { HomeUiNavigates.Idle }
    }

    private fun navigateToDetail(note: NotePresentationModel) {
        _navigates.update {
            HomeUiNavigates.NavigateToEditNote(
                mustConsume = true,
                taskIndex = noteList.value.notes.indexOf(note),
                notePresentationModel = note
            )
        }

        onSelectionEnd()
    }

    fun onNavigateToDetailCompleted() {
        _navigates.update { HomeUiNavigates.Idle }
    }

    //endregion

    //region Reordering

    fun onReorderedNotes(reorderedTaskList: List<NotePresentationModel>) {
//        if (reorderedTaskList == noteList.value.notes) return
        reorderTasks(reorderedTaskList)
    }

    fun onReorderNotesCancelled() {
        onSelectionDismissed()
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
            delay(100) //Give time to database to have new values.
            onSelectionEnd()
        }
    }

    //endregion

    //region Messaging

    private fun showErrorMessage(exception: Exception) {
        messageManager.showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    //endregion

    //region Selection mode

    fun onSelection() {
        val sizeSelected = getSelectedTasks().size

        selectionModeActive.update {
            SelectionModeActive(
                size = sizeSelected,
                selectedIds = getSelectedTasks().map { it.id }
            )
        }
    }

    private fun onSelectionEnd() {
        selectionModeActive.update { SelectionModeActive() }
    }

    fun onSelectionDismissed() {
        onSelectionEnd()
    }

    private fun getSelectedTasks() = noteList.value.notes.filter { it.isSelected }

    //endregion

    //region Layout change

    fun onLayoutChange(noteLayout: NoteLayout) {
        saveLayoutPreference(noteLayout)
    }

    private fun saveLayoutPreference(noteLayout: NoteLayout) {
        viewModelScope.launch {
            saveUserPrefUseCase(SaveUserPrefUseCase.Params(UserPreferences(noteLayout)))
        }
    }

    //endregion

    //region Sharing

    fun onShare() {
        val tasksToCopy = getSelectedTasks()

        noteActions.update {
            it.copy(shareNoteAsString = tasksToCopy.toCopyString())
        }

        onSelectionEnd()
    }

    fun onShareCompleted() {
        noteActions.update { it.copy(shareNoteAsString = null) }
    }

    //endregion

    //region Copy

    fun onCopy() {
        val tasksToCopy = getSelectedTasks()

        noteActions.update {
            it.copy(
                copyToClipboard = tasksToCopy.toCopyString(),
            )
        }

        onSelectionEnd()
    }

    fun onCopiedCompleted() {
        noteActions.update { it.copy(copyToClipboard = null) }
    }

    fun onSearch(key: String) {
        this.key.update { key }
    }

    //endregion

}


