package brillembourg.notes.simple.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import brillembourg.notes.simple.data.UserPreferencesRepositoryImp.PreferencesKeys.IS_GRID
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.domain.use_cases.GetUserPreferencesUseCase
import brillembourg.notes.simple.domain.use_cases.SaveUserPreferencesUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImp(val context: Context) : UserPrefRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun getUserPreferences(params: GetUserPreferencesUseCase.Params): Flow<Resource<GetUserPreferencesUseCase.Result>> {
        return context.dataStore.data.map { preferences ->
            val isGrid = preferences[IS_GRID] ?: false
            Log.e("User Preferences", isGrid.toString())
            val userPreferences =
                UserPreferences(if (isGrid) NoteLayout.Grid else NoteLayout.Vertical)
            Resource.Success(GetUserPreferencesUseCase.Result(userPreferences))
        }
    }

    override suspend fun saveUserPreferences(params: SaveUserPreferencesUseCase.Params) {
        context.dataStore.edit { preferences ->
            preferences[IS_GRID] = when (params.userPreferences.notesLayout) {
                NoteLayout.Vertical -> false
                NoteLayout.Grid -> true
            }
        }
    }


    private object PreferencesKeys {
        val IS_GRID = booleanPreferencesKey("is_grid")
    }
}


