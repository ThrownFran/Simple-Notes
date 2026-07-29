package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.usecases.categories.CreateCategoryUseCase
import brillembourg.notes.simple.domain.usecases.categories.DeleteCategoriesUseCase
import brillembourg.notes.simple.domain.usecases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.domain.usecases.categories.ReorderCategoriesUseCase
import brillembourg.notes.simple.domain.usecases.categories.SaveCategoryUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun create(params: CreateCategoryUseCase.Params): Resource<CreateCategoryUseCase.Result>

    suspend fun save(params: SaveCategoryUseCase.Params): Resource<SaveCategoryUseCase.Result>

    suspend fun deleteMultiple(params: DeleteCategoriesUseCase.Params): Resource<DeleteCategoriesUseCase.Result>

    suspend fun reorderList(params: ReorderCategoriesUseCase.Params): Resource<ReorderCategoriesUseCase.Result>

    fun getList(params: GetCategoriesUseCase.Params): Flow<Resource<GetCategoriesUseCase.Result>>
}
