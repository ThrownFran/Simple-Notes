package brillembourg.notes.simple.domain.use_cases.user

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.domain.use_cases.categories.GetCategoriesUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class GetFilterByCategoriesUseCase @Inject constructor(
    private val repository: UserPrefRepository,
    private val schedulers: Schedulers
) {

    class Params(val getCategoriesFlow: Flow<Resource<GetCategoriesUseCase.Result>>)
    class CategoriesIds(val categoryIds: List<Long>)
    class Result(val categories: List<Category>)

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getFilter(params)
            .flowOn(schedulers.defaultDispatcher())
            .transform { categoriesIdsResult ->
                if (categoriesIdsResult is Resource.Success) {
                    params.getCategoriesFlow
                        .flowOn(schedulers.defaultDispatcher())
                        .collect { allCategoriesResult: Resource<GetCategoriesUseCase.Result> ->
                            emitCategoryModel(allCategoriesResult, categoriesIdsResult)
                        }
                } else {
                    throw IllegalArgumentException("Category id not found")
                }
            }
    }

    private suspend fun FlowCollector<Resource<Result>>.emitCategoryModel(
        allCategoriesResult: Resource<GetCategoriesUseCase.Result>,
        filteredIds: Resource.Success<CategoriesIds>
    ) {
        when (allCategoriesResult) {
            is Resource.Success ->
                emit(
                    Resource.Success(
                        buildCategories(
                            filteredIds.data.categoryIds,
                            allCategoriesResult.data.categoryList
                        )
                    )
                )
            is Resource.Error -> emit(Resource.Error(allCategoriesResult.exception))
            is Resource.Loading -> emit(Resource.Loading())
        }
    }

    private fun buildCategories(
        filteredIds: List<Long>,
        availableCategories: List<Category>
    ): Result {
        return Result(availableCategories.filter { category -> filteredIds.contains(category.id) }
        )
    }


}