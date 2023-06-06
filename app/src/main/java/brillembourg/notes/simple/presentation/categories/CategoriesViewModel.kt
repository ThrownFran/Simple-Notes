package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.categories.CreateCategoryUseCase
import brillembourg.notes.simple.domain.use_cases.categories.DeleteCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.ReorderCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.SaveCategoryUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.home.delete.NoteDeletionState
import brillembourg.notes.simple.presentation.home.stopTimeoutMillis
import brillembourg.notes.simple.presentation.trash.SelectionModeActive
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoriesUseCase: DeleteCategoriesUseCase,
    private val reorderCategoriesUseCase: ReorderCategoriesUseCase,
    private val messageManager: MessageManager
) : ViewModel() {

    private val selectionMode: MutableStateFlow<SelectionModeActive> = MutableStateFlow(
        SelectionModeActive()
    )
    private val categoryList: StateFlow<CategoryList> =
        getCategoriesUseCase(GetCategoriesUseCase.Params())
            .combine(selectionMode) { result, selectionMode ->
                result to selectionMode
            }
            .transform { pair ->
                val result = pair.first
                val selectionMode = pair.second
                when (result) {
                    is Resource.Success ->
                        emit(
                            CategoryList(
                                data = result.data.categoryList
                                    .map { category ->
                                        category.toPresentation().apply {
                                            isSelected =
                                                selectionMode.selectedIds.contains(category.id)
                                        }
                                    }
                                    .sortedBy { it.order }
                                    .asReversed(),
                                mustRender = true
                            )
                        )

                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
                initialValue = CategoryList()
            )

    private val createCategory: MutableStateFlow<CreateCategory> =
        MutableStateFlow(CreateCategory())
    private val deleteConfirmation: MutableStateFlow<NoteDeletionState.ConfirmDeleteDialog?> =
        MutableStateFlow(null)

    val categoryUiState: StateFlow<CategoriesUiState> = combine(
        categoryList,
        selectionMode,
        createCategory,
        deleteConfirmation
    )
    { categoryList, selectionMode, createCategory, deleteConfirmation ->
        CategoriesUiState(
            categoryList = categoryList,
            selectionMode = selectionMode,
            deleteConfirmation = deleteConfirmation,
            createCategory = createCategory
        )
    }
        .distinctUntilChanged()
        .debounce(250)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = CategoriesUiState()
        )

    //endregion

    //region Messages

    private fun showErrorMessage(exception: Exception) {
        messageManager.showError(exception)
    }

    private fun showMessage(uiText: UiText) = messageManager.showMessage(uiText)

    //endregion

    //////////////// CREATE

    //region Create

    fun onCreateCategory(providedName: String?) {

        if (providedName.isNullOrEmpty()) {
            showMessage(UiText.CategoryNameEmpty)
            return
        }

        createCategory(providedName)

        createCategory.update { CreateCategory(isEnabled = false, name = "") }
    }

    private fun createCategory(name: String) {

        viewModelScope.launch {
            when (val result = createCategoryUseCase(CreateCategoryUseCase.Params(name))) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    //endregion

    //region Selection

    fun onSelection(isSelected: Boolean, id: Long) {
        val selectedIds: MutableList<Long> = selectionMode.value.selectedIds.toMutableList()
        if (isSelected) {
            selectedIds.add(id)
        } else {
            selectedIds.remove(id)
        }
        val isActive = selectedIds.size > 0

        selectionMode.update {
            SelectionModeActive(
                isActive = isActive,
                selectedIds = selectedIds,
                size = selectedIds.size
            )
        }
    }

    fun onSelectionDismissed() {
        selectionMode.update { SelectionModeActive() }
    }

    //endregion

    //region Delete

    fun onDeleteConfirmCategories() {
        deleteConfirmation.update {
            NoteDeletionState.ConfirmDeleteDialog(
                selectionMode.value.size
            )
        }
    }

    fun onDismissConfirmDeleteShown() {
        deleteConfirmation.update { null }
    }

    fun onDeleteCategories() {
        deleteCategories(selectionMode.value.selectedIds)
        deleteConfirmation.update { null }
        selectionMode.update { SelectionModeActive() }
    }

    private fun deleteCategories(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = DeleteCategoriesUseCase.Params(tasksToDeleteIds)
            when (val result = deleteCategoriesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    //endregion

    //region Reordered

    fun onReorderedCategories(reorderedCategoryList: List<CategoryPresentationModel>) {
        if (reorderedCategoryList == categoryList.value.data) return
        reorderCategories(reorderedCategoryList)
    }

    private fun reorderCategories(reorderedTaskList: List<CategoryPresentationModel>) {
        viewModelScope.launch {
            val params = ReorderCategoriesUseCase.Params(reorderedTaskList.map {
                it.toDomain()
            })

            when (val result = reorderCategoriesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }

            selectionMode.update { SelectionModeActive() }
        }
    }

    fun onReorderCategoriesCancelled() {
        onSelectionDismissed()
    }

    //endregion

    //region Save

    fun onSave(newName: String, categoryPresentationModel: CategoryPresentationModel) {
        val categoryRenamed = categoryPresentationModel.copy(name = newName).toDomain()

        viewModelScope.launch {
            val params = SaveCategoryUseCase.Params(categoryRenamed)
            when (val result = saveCategoryUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    //endregion


}