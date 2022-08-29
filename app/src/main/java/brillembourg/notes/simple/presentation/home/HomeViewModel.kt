package brillembourg.notes.simple.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.ArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.DeleteNotesUseCase
import brillembourg.notes.simple.domain.use_cases.GetNotesUseCase
import brillembourg.notes.simple.domain.use_cases.ReorderNotesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import brillembourg.notes.simple.presentation.categories.toDomain
import brillembourg.notes.simple.presentation.categories.toPresentation
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.models.toCopyString
import brillembourg.notes.simple.presentation.models.toDomain
import brillembourg.notes.simple.presentation.models.toPresentation
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
    private val saveFilterByCategoriesUseCase: SaveFilterByCategoriesUseCase,
    private val getFilteredCategoriesUseCase: GetFilterByCategoriesUseCase,
    private val getAvailableCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val uiStateKey = "home_ui_state"

    private val _homeUiState = MutableStateFlow(
        getSavedUiState() ?: HomeUiState(
//        selectFilterCategories = SelectFilterCategories(
//            categories =
//        )
        )
    )
    val homeUiState = _homeUiState.asStateFlow()

    private fun getSavedUiState(): HomeUiState? = savedStateHandle.get<HomeUiState>(uiStateKey)

    val availableCategoriesFlow =
        getAvailableCategoriesUseCase.invoke(GetCategoriesUseCase.Params())

    init {
        observePreferences()
        saveChangesInSavedStateObserver()
        observeCategories()
    }

    //region Categories

    private fun observeCategories() {

        availableCategoriesFlow.onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _homeUiState.update { uiState ->
                        uiState.copy(
                            selectFilterCategories = _homeUiState.value.selectFilterCategories.copy(
                                categories = result.data.categoryList
                                    .map { it.toPresentation() }
                                    .toDiplayOrder(),
                                isFilterCategoryMenuAvailable = true
                            )
                        )
                    }
                }
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit

            }
        }
            .launchIn(viewModelScope)

        getFilteredCategoriesUseCase.invoke(
            GetFilterByCategoriesUseCase.Params(availableCategoriesFlow)
        )
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _homeUiState.update { uiState ->
                            uiState.copy(
                                filteredCategories = result.data.categories
                                    .map { it.toPresentation() }
                                    .toDiplayOrder()
                            )
                        }
                    }
                    is Resource.Error -> {
                        showErrorMessage(result.exception)
                    }
                    is Resource.Loading -> {
                        Unit
                    }
                }
                observeNoteList()
            }
            .launchIn(viewModelScope)
    }


    fun onNavigateToCategories() {
        _homeUiState.update {
            it.copy(
                selectFilterCategories = it.selectFilterCategories.copy(
                    navigate = true,
                )
            )
        }
    }

    fun onHideCategories() {
        _homeUiState.update {
            it.copy(
                selectFilterCategories = it.selectFilterCategories.copy(
                    isShowing = false
                )
            )
        }
    }

    fun onCategoriesShowing() {
        _homeUiState.update {
            it.copy(
                selectFilterCategories = it.selectFilterCategories.copy(
                    isShowing = true,
                    navigate = false
                )
            )
        }
    }

    fun onCategoryChecked(category: CategoryPresentationModel, isChecked: Boolean) {
        val categoryUpdated = category.copy(isSelected = isChecked)

        val currentCategories: List<CategoryPresentationModel> =
            _homeUiState.value.filteredCategories

        val categoriesUpdated =
            if (isChecked) currentCategories + categoryUpdated
            else currentCategories - categoryUpdated

        viewModelScope.launch {
            val params = SaveFilterByCategoriesUseCase.Params(categoriesUpdated.map { it.id })
            saveFilterByCategoriesUseCase.invoke(params)
            observeCategories()
        }
    }

    //endregion

    private fun saveChangesInSavedStateObserver() {
        viewModelScope.launch {
            homeUiState.collect {
                savedStateHandle[uiStateKey] = it
            }
        }
    }

    private fun observePreferences() {
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

    private fun observeNoteList() {
        getNotesUseCase(GetNotesUseCase.Params(_homeUiState.value.filteredCategories.map { it.toDomain() }))
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _homeUiState.update { uiState ->
                            uiState.copy(
                                noteList = NoteList(
                                    notes = result.data.noteList
                                        .map { noteWithCategories ->
                                            noteWithCategories.toPresentation(dateProvider).apply {
                                                this.isSelected =
                                                    isNoteSelectedInUi(
                                                        uiState,
                                                        noteWithCategories.note
                                                    )
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
                showDeleteNotesConfirmation = DeleteCategoriesConfirmation(
                    tasksToDeleteSize = getSelectedTasks().size
                )
            )
        }
    }

    fun onShare() {
        val tasksToCopy = getSelectedTasks()
        _homeUiState.update {
            it.copy(
                shareNoteAsString = tasksToCopy.toCopyString(),
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


