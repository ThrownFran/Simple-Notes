package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.categories.*
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun createCategory(params: CreateCategoryUseCase.Params): Resource<CreateCategoryUseCase.Result>
    suspend fun saveCategory(params: SaveCategoryUseCase.Params): Resource<SaveCategoryUseCase.Result>
    suspend fun deleteCategories(params: DeleteCategoriesUseCase.Params): Resource<DeleteCategoriesUseCase.Result>
    suspend fun reorderCategories(params: ReorderCategoriesUseCase.Params): Resource<ReorderCategoriesUseCase.Result>
    fun getCategories(params: GetCategoriesUseCase.Params): Flow<Resource<GetCategoriesUseCase.Result>>

}