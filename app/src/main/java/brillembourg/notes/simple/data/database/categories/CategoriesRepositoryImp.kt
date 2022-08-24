package brillembourg.notes.simple.data.database.categories

import brillembourg.notes.simple.domain.repositories.CategoryRepository
import brillembourg.notes.simple.domain.use_cases.categories.*
import brillembourg.notes.simple.util.GetCategoriesException
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class CategoriesRepositoryImp(
    private val database: CategoriesDatabase,
) : CategoryRepository {


    override suspend fun create(params: CreateCategoryUseCase.Params): Resource<CreateCategoryUseCase.Result> {
        return safeCall {
            val category = database.create(params.name).toDomain()
            Resource.Success(CreateCategoryUseCase.Result(category, UiText.CategoryCreated))
        }
    }

    override suspend fun save(params: SaveCategoryUseCase.Params): Resource<SaveCategoryUseCase.Result> {
        return safeCall {
            database.save(params.category.toEntity())
            Resource.Success(SaveCategoryUseCase.Result(UiText.CategoryUpdated))
        }
    }

    override suspend fun deleteMultiple(params: DeleteCategoriesUseCase.Params): Resource<DeleteCategoriesUseCase.Result> {
        return safeCall {
            database.deleteMultiple(params.ids)
            val message =
                if (params.ids.size > 1) UiText.CategoriesDeleted
                else UiText.CategoryDeleted
            Resource.Success(DeleteCategoriesUseCase.Result(message))
        }
    }

    override suspend fun reorderList(params: ReorderCategoriesUseCase.Params): Resource<ReorderCategoriesUseCase.Result> {
        return safeCall {
            database.saveReordering(params.categoryList.map { it.toEntity() })
            Resource.Success(ReorderCategoriesUseCase.Result(UiText.CategoriesReordered))
        }
    }

    override fun getList(params: GetCategoriesUseCase.Params): Flow<Resource<GetCategoriesUseCase.Result>> {
        return database.getList()
            .debounce(200)
            .distinctUntilChanged()
            .transform {
                try {
                    val categoriesDomain = it.map { categoryEntity -> categoryEntity.toDomain() }
                    val result = GetCategoriesUseCase.Result(categoriesDomain)
                    emit(Resource.Success(result))
                } catch (e: Exception) {
                    emit(Resource.Error(GetCategoriesException(e.message)))
                }
            }
    }
}