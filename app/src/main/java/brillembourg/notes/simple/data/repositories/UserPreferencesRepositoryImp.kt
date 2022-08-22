package brillembourg.notes.simple.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import brillembourg.notes.simple.data.repositories.UserPreferencesRepositoryImp.PreferencesKeys.IS_GRID
import brillembourg.notes.simple.data.repositories.UserPreferencesRepositoryImp.PreferencesKeys.LAYOUT_GRID
import brillembourg.notes.simple.data.repositories.UserPreferencesRepositoryImp.PreferencesKeys.LAYOUT_VERTICAL
import brillembourg.notes.simple.data.repositories.UserPreferencesRepositoryImp.PreferencesKeys.PREFERENCE_STORE
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.domain.use_cases.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.SaveUserPrefUseCase
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepositoryImp(val context: Context) : UserPrefRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_STORE)

    override fun getUserPreferences(params: GetUserPrefUseCase.Params): Flow<Resource<GetUserPrefUseCase.Result>> {
        return context.dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                try {
                    val noteLayout = preferences[IS_GRID]?.toNoteLayout()
                    val userPreferences =
                        if (noteLayout == null) UserPreferences()
                        else UserPreferences(noteLayout)
                    Resource.Success(GetUserPrefUseCase.Result(userPreferences))
                } catch (e: Exception) {
                    Resource.Error(e)
                }
            }
    }

    override suspend fun saveUserPreferences(params: SaveUserPrefUseCase.Params) {
        context.dataStore.edit { preferences ->
            preferences[IS_GRID] = params.userPreferences.notesLayout.toStringPreferenceValue()
        }
    }

    private fun NoteLayout.toStringPreferenceValue(): Int {
        return when (this) {
            NoteLayout.Vertical -> LAYOUT_VERTICAL
            NoteLayout.Grid -> LAYOUT_GRID
        }
    }

    private fun Int.toNoteLayout(): NoteLayout {
        return when (this) {
            LAYOUT_VERTICAL -> NoteLayout.Vertical
            LAYOUT_GRID -> NoteLayout.Grid
            else -> throw IllegalArgumentException("Note layout value invalid: $this")
        }
    }


    private object PreferencesKeys {
        const val PREFERENCE_STORE = "user-preferences"

        const val LAYOUT_VERTICAL = 1
        const val LAYOUT_GRID = 2

        val IS_GRID = intPreferencesKey("is_grid")
    }
}


