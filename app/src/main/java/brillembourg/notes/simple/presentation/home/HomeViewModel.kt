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
import kotlinx.coroutines.Job
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
    private val _homeUiState = MutableStateFlow(getSavedUiState() ?: HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    //Job references to allow cancellation
    private var filteredCategoriesJob: Job? = null
    private var categorieAvailableJob: Job? = null
    private var getNotesJob: Job? = null

    init {
        observePreferences()
        saveChangesInSavedStateObserver()
        observeCategories {
            observeNoteList()
        }
    }

    //region Note list

    private fun observeNoteList() {
        getNotesJob?.cancel()
        getNotesJob =
            getNotesUseCase(GetNotesUseCase.Params(_homeUiState.value.filteredCategories.map { it.toDomain() }))
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _homeUiState.update { uiState ->

                                uiState.copy(
                                    noteList = NoteList(
                                        notes = result.data.noteList
                                            .map { noteWithCategories ->
                                                noteWithCategories.toPresentation(dateProvider)
                                                    .apply {
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
                }
                .launchIn(viewModelScope)
    }

    fun onNoteClick(it: NotePresentationModel) {
        navigateToDetail(it)
    }

    private fun isNoteSelectedInUi(
        uiState: HomeUiState,
        note: Note
    ) = (uiState.noteList.notes
        .firstOrNull() { note.id == it.id }?.isSelected
        ?: false)
    //endregion

    //region Categories

    private fun observeCategories(onFilteredCategoriesLoaded: (() -> Unit)? = null) {

//        val availableCategoriesFlow =
//            getAvailableCategoriesUseCase.invoke(GetCategoriesUseCase.Params())

        categorieAvailableJob?.cancel()
        categorieAvailableJob =
            getAvailableCategoriesUseCase.invoke(GetCategoriesUseCase.Params()).onEach { result ->
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

        filteredCategoriesJob?.cancel()
        filteredCategoriesJob =
            getFilteredCategoriesUseCase.invoke(GetFilterByCategoriesUseCase.Params())
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
                    onFilteredCategoriesLoaded?.invoke()
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
            observeCategories {
                observeNoteList()
            }
        }
    }

    //endregion

    //region Saved state

    private fun saveChangesInSavedStateObserver() {
        viewModelScope.launch {
            homeUiState.collect {
                savedStateHandle[uiStateKey] = it
            }
        }
    }

    private fun getSavedUiState(): HomeUiState? = savedStateHandle.get<HomeUiState>(uiStateKey)


    //endregion

    //region Preferences

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

    //endregion

    //region Navigation

    fun onAddNoteClick(content: String? = null) {
        navigateToAddNote(content)
    }

    private fun navigateToAddNote(content: String?) {
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

    //endregion

    //region Reordering

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

    //endregion

    //region Messaging

    private fun showErrorMessage(exception: Exception) {
        messageManager.showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    //endregion

    //region Delete

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

    fun onDeleteConfirm() {
        _homeUiState.update {
            it.copy(
                showDeleteNotesConfirmation = DeleteCategoriesConfirmation(
                    tasksToDeleteSize = getSelectedTasks().size
                )
            )
        }
    }

    fun onDismissConfirmDeleteShown() {
        _homeUiState.update { it.copy(showDeleteNotesConfirmation = null) }
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

    //endregion

    //region Archive
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

    //endregion

    //region Selection mode

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

    //endregion

    //region Layout change

    fun onLayoutChange(noteLayout: NoteLayout) {
        _homeUiState.update { it.copy(noteLayout = noteLayout) }
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

    //endregion

    //region Copy

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

    //endregion

}


