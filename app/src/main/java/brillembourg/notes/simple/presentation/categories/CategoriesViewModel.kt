package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.categories.CreateCategoryUseCase
import brillembourg.notes.simple.domain.use_cases.categories.DeleteCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.SaveCategoryUseCase
import brillembourg.notes.simple.presentation.trash.MessageManager
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
                            list = result.data.categoryList
                                .map { category -> category.toPresentation() }
                                .sortedBy { it.order }
                                .asReversed()
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
                createCategory = true,
                selectionModeActive = null
            )
        }
    }

    fun onShowCreateCategoryCompleted() {
        _categoryUiState.update {
            it.copy(createCategory = false)
        }
    }

    fun onCreateCategory(name: String) {

        viewModelScope.launch {
            val result = createCategoryUseCase(CreateCategoryUseCase.Params(name))
            when (result) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }


}