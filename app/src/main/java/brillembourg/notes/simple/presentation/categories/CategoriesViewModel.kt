package brillembourg.notes.simple.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import brillembourg.notes.simple.domain.use_cases.categories.CreateCategoryUseCase
import brillembourg.notes.simple.domain.use_cases.categories.DeleteCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.categories.SaveCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val deleteCategoriesUseCase: DeleteCategoriesUseCase
) : ViewModel() {


}