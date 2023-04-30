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
import brillembourg.notes.simple.domain.use_cases.user.GetFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import brillembourg.notes.simple.presentation.categories.toDomain
import brillembourg.notes.simple.presentation.categories.toPresentation
import brillembourg.notes.simple.presentation.home.delete.DeleteAndArchiveManager
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


val stopTimeoutMillis: Long = 5_000

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
    deleteNotesUseCase: DeleteNotesUseCase,
    getFilteredCategoriesUseCase: GetFilterByCategoriesUseCase,
    getAvailableCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val uiStateKey = "home_ui_state"

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    private val _navigates: MutableStateFlow<HomeUiNavigates> =
        MutableStateFlow(HomeUiNavigates.Idle)
    val navigates = _navigates.asStateFlow()

    private val _key: MutableStateFlow<String> = MutableStateFlow("")
    val allCategories: StateFlow<List<CategoryPresentationModel>> =
        initAllCategories(getAvailableCategoriesUseCase)

    val filteredCategories: StateFlow<List<CategoryPresentationModel>> =
        initFilteredCategories(getFilteredCategoriesUseCase)

    val noteList: StateFlow<NoteList> = initNoteList()

    val deleteAndArchiveManager = DeleteAndArchiveManager(
        archiveNotesUseCase = archiveNotesUseCase,
        deleteNotesUseCase = deleteNotesUseCase,
        messageManager = messageManager,
        noteList = noteList,
        coroutineScope = viewModelScope,
        onDismissSelectionMode = {
            _homeUiState.update { it.copy(selectionModeActive = null) }
        }
    )

    private fun getSavedUiState(): HomeUiState? = savedStateHandle.get<HomeUiState>(uiStateKey)

    init {
        getSavedUiState()?.let { _homeUiState.value = it }
        observePreferences()
        saveChangesInSavedStateObserver()
    }

    private fun initNoteList() = combine(_key, filteredCategories) { key, filteredCategories ->
        GetNotesUseCase.Params(
            filterByCategories = filteredCategories.map { it.toDomain() },
            keySearch = key
        )
    }.distinctUntilChanged()
        .debounce(300)
        .flatMapLatest { params ->
            getNotesUseCase(params)
        }.transform { result ->
            when (result) {
                is Resource.Success -> {
                    emit(NoteList(
                        notes = result.data.noteList
                            .map { noteWithCategories ->
                                noteWithCategories.toPresentation(dateProvider)
                                    .apply {
                                        this.isSelected =
                                            isNoteSelectedInUi(noteWithCategories.note)
                                    }
                            }
                            .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                            .asReversed(),
                        mustRender = true
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
                        emit(result.data.categoryList
                            .map { it.toPresentation() }
                            .toDiplayOrder()
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

    //region Note list

    fun onNoteClick(it: NotePresentationModel) {
        navigateToDetail(it)
    }

    private fun isNoteSelectedInUi(
        note: Note
    ) = (noteList.value.notes
        .firstOrNull() { note.id == it.id }?.isSelected
        ?: false)
    //endregion

    //region Categories

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

    //region Saved state

    private fun saveChangesInSavedStateObserver() {
        viewModelScope.launch {
            homeUiState.collect {
                savedStateHandle[uiStateKey] = it
            }
        }
    }

//    private fun getSavedUiState(): HomeUiState? = savedStateHandle.get<HomeUiState>(uiStateKey)


    //endregion

    //region Preferences

    private fun observePreferences() {
        viewModelScope.launch {
            getUserPrefUseCase(GetUserPrefUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _homeUiState.update { it.copy(noteLayout = result.data.preferences.noteLayout) }
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
        _navigates.update { HomeUiNavigates.NavigateToAddNote(content) }
        _homeUiState.update { it.copy(selectionModeActive = null) }
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

        _homeUiState.update {
            it.copy(selectionModeActive = null)
        }
    }

    fun onNavigateToDetailCompleted() {
        _navigates.update { HomeUiNavigates.Idle }
    }

    //endregion

    //region Reordering

    fun onReorderedNotes(reorderedTaskList: List<NotePresentationModel>) {
        _homeUiState.update { it.copy(selectionModeActive = null) }

        if (reorderedTaskList == noteList.value.notes) return
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

    private fun getSelectedTasks() = noteList.value.notes.filter { it.isSelected }

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

    fun onSearch(key: String) {
        _key.update { key }
    }

    //endregion

}


