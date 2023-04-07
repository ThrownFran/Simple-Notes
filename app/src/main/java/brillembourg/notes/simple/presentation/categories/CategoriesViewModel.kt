package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.categories.*
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.home.HomeDialogState
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoriesUseCase: DeleteCategoriesUseCase,
    private val reorderCategoriesUseCase: ReorderCategoriesUseCase,
    private val messageManager: MessageManager
) : ViewModel() {

    private val _categoryUiState = MutableStateFlow(CategoriesUiState())
        .apply { observeCategoryList() }
    val categoryUiState = _categoryUiState.asStateFlow()

    //region List

    private fun observeCategoryList() {
        getCategoriesUseCase(GetCategoriesUseCase.Params())
            .onEach { result ->
                when (result) {
                    is Resource.Success -> _categoryUiState.update { uiState ->
                        uiState.copy(
                            categoryList = CategoryList(
                                data = result.data.categoryList
                                    .map { category -> category.toPresentation() }
                                    .sortedBy { it.order }
                                    .asReversed(),
                                mustRender = true
                            )
                        )
                    }

                    is Resource.Error -> showErrorMessage(result.exception)
                    is Resource.Loading -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

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

        _categoryUiState.update {
            it.copy(createCategory = it.createCategory.copy(isEnabled = false, name = ""))
        }
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

    fun onSelection() {
        val sizeSelected = getSelectedCategories().size

        _categoryUiState.update {
            it.copy(
                selectionMode = SelectionMode(
                    size = sizeSelected
                )
            )
        }
    }

    fun onSelectionDismissed() {
        _categoryUiState.update { it.copy(selectionMode = null) }
    }

    private fun getSelectedCategories(): List<CategoryPresentationModel> {
        return categoryUiState.value.categoryList.data.filter { it.isSelected }
    }

    //endregion

    //region Delete

    fun onDeleteConfirmCategories() {
        _categoryUiState.update {
            it.copy(
                deleteConfirmation = HomeDialogState.DeleteCategoriesConfirmation(
                    getSelectedCategories().size
                )
            )
        }
    }

    fun onDismissConfirmDeleteShown() {
        _categoryUiState.update {
            it.copy(
                deleteConfirmation = null
            )
        }
    }

    fun onDeleteCategories() {
        val tasksToDeleteIds = getSelectedCategories().map { it.id }
        deleteCategories(tasksToDeleteIds)

        _categoryUiState.update {
            it.copy(
                deleteConfirmation = null,
                selectionMode = null
            )
        }
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
        _categoryUiState.update {

            val categoryList: List<CategoryPresentationModel> =
                _categoryUiState.value.categoryList.data
            categoryList.forEach { it.isSelected = false }

            it.copy(
                selectionMode = null,
                categoryList = _categoryUiState.value.categoryList.copy(
                    data = categoryList,
                    mustRender = false
                )
            )
        }

        if (reorderedCategoryList == _categoryUiState.value.categoryList.data) return
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