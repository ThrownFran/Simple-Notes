package brillembourg.notes.simple.domain.use_cases.categories

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.repositories.CategoryRepository
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val schedulers: Schedulers
) {

    operator fun invoke(params: Params): Flow<Resource<Result>> {
        return repository.getList(params)
            .flowOn(schedulers.defaultDispatcher())
    }

    class Params
    class Result(val categoryList: List<Category>)
}