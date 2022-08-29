package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.user.GetFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserPrefRepository {
    fun getUserPreferences(params: GetUserPrefUseCase.Params): Flow<Resource<GetUserPrefUseCase.Result>>
    suspend fun saveUserPreferences(params: SaveUserPrefUseCase.Params)
    fun getFilter(params: GetFilterByCategoriesUseCase.Params): Flow<Resource<GetFilterByCategoriesUseCase.CategoriesIds>>
    suspend fun saveFilter(params: SaveFilterByCategoriesUseCase.Params)
}