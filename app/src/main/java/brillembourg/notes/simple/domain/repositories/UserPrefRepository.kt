package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.SaveUserPreferencesUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserPrefRepository {
    fun getUserPreferences(params: GetUserPrefUseCase.Params): Flow<Resource<GetUserPrefUseCase.Result>>
    suspend fun saveUserPreferences(params: SaveUserPreferencesUseCase.Params)
}