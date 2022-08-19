package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.GetUserPreferencesUseCase
import brillembourg.notes.simple.domain.use_cases.SaveUserPreferencesUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserPrefRepository {
    fun getUserPreferences(params: GetUserPreferencesUseCase.Params): Flow<Resource<GetUserPreferencesUseCase.Result>>
    suspend fun saveUserPreferences(params: SaveUserPreferencesUseCase.Params)
}