package brillembourg.notes.simple.domain.use_cases.categories

import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.domain.repositories.CategoryRepository
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val schedulers: Schedulers
) {

    suspend operator fun invoke(params: Params): Resource<Result> =
        withContext(schedulers.defaultDispatcher()) {
            repository.create(params)
        }

    class Params(val name: String)

    class Result(
        val category: Category,
        val message: UiText
    )

}