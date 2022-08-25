package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.categories.*
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.home.DeleteCategoriesConfirmation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
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
    val categoryUiState = _categoryUiState.asStateFlow()

    init {
        observeCategoryList()
    }

    private fun observeCategoryList() {
        getCategoriesUseCase(GetCategoriesUseCase.Params())
            .onEach { result ->
                when (result) {
                    is Resource.Success -> _categoryUiState.update {
                        it.copy(
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
                    is Resource.Loading -> TODO()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun showErrorMessage(exception: Exception) {
        messageManager.showMessage(getMessageFromError(exception))
    }

    private fun showMessage(uiText: UiText) = messageManager.showMessage(uiText)


    //////////////// CREATE

    fun onShowCreateCategory() {
        _categoryUiState.update {
            it.copy(
                createCategory = it.createCategory.copy(isEnabled = true),
                selectionMode = null
            )
        }
    }

    fun onCreateCategory() {
        val name = categoryUiState.value.createCategory?.name

        if (name.isNullOrEmpty()) {
            showMessage(UiText.DynamicString("No name"))
            return
        }

        createCategory(name)

        _categoryUiState.update {
            it.copy(createCategory = it.createCategory.copy(isEnabled = false, name = ""))
        }
        _categoryUiState.value.createCategory.clear()
    }

    fun onShowCreateCategoryDismiss() {
        _categoryUiState.update {
            it.copy(createCategory = it.createCategory.copy(isEnabled = false, name = ""))
        }
    }

    private fun createCategory(name: String) {

        viewModelScope.launch {
            val result = createCategoryUseCase(CreateCategoryUseCase.Params(name))
            when (result) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

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

    fun onDeleteConfirmCategories() {
        _categoryUiState.update {
            it.copy(
                deleteConfirmation = DeleteCategoriesConfirmation(
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

//    fun onEdit() {
//        _categoryUiState.update { it.copy(isEditing = true) }
//    }

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

//    fun onSave() {
//        val categoryList = categoryUiState.value.categoryList.data
//            .onEach { it.isEditing = false }
//
//        _categoryUiState.update { uiState ->
//            uiState.copy(
//                isEditing = false,
//                categoryList = uiState.categoryList.copy(
//                    data = categoryList,
//                    mustRender = true
//                )
//            )
//        }

    //TODO SAVE ACTUAL
//    }


}