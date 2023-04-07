package brillembourg.notes.simple.data.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.FILTER_IDS
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.IS_GRID
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.LAYOUT_GRID
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.LAYOUT_VERTICAL
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.PREFERENCE_STORE
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.THEME
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.THEME_DARK
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.THEME_LIGHT
import brillembourg.notes.simple.data.user.UserPreferencesRepositoryImp.PreferencesKeys.THEME_SYSTEM
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.domain.use_cases.user.GetFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveFilterByCategoriesUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.presentation.settings.ThemeMode
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepositoryImp(val context: Context) : UserPrefRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_STORE)

    override fun getFilter(params: GetFilterByCategoriesUseCase.Params): Flow<Resource<GetFilterByCategoriesUseCase.CategoriesIds>> {
        return context.dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences: Preferences ->
                val set: Set<String>? = preferences[FILTER_IDS]
                val ids = set?.toList()?.map { it.toLong() } ?: emptyList()
                Resource.Success(
                    GetFilterByCategoriesUseCase.CategoriesIds(ids)
                )
            }
    }

    override suspend fun saveFilter(params: SaveFilterByCategoriesUseCase.Params) {
        context.dataStore.edit { preferences ->
            val set: MutableSet<String> = HashSet()
            set.addAll(params.categoryIds.map { it.toString() })
            preferences[FILTER_IDS] = set
        }
    }

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
                    val theme = preferences[THEME]?.toThemeMode()

                    val userPreferences = UserPreferences(
                        _notesLayout = noteLayout ?: NoteLayout.Vertical,
                        _theme = theme ?: ThemeMode.Light
                    )
                    Resource.Success(GetUserPrefUseCase.Result(userPreferences))
                } catch (e: Exception) {
                    Resource.Error(e)
                }
            }
    }

    override suspend fun saveUserPreferences(params: SaveUserPrefUseCase.Params) {
        context.dataStore.edit { preferences ->
            params.userPreferences._notesLayout?.let {
                preferences[IS_GRID] = it.toStringPreferenceValue()
            }
            params.userPreferences._theme?.let { preferences[THEME] = it.toPreferenceValue() }
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

    private fun Int.toThemeMode(): ThemeMode {
        return when (this) {
            THEME_SYSTEM -> ThemeMode.System
            THEME_LIGHT -> ThemeMode.Light
            THEME_DARK -> ThemeMode.Dark
            else -> throw IllegalArgumentException("Theme value invalid: $this")
        }
    }

    private fun ThemeMode.toPreferenceValue(): Int {
        return when (this) {
            ThemeMode.System -> THEME_SYSTEM
            ThemeMode.Light -> THEME_LIGHT
            ThemeMode.Dark -> THEME_DARK
        }
    }

    private object PreferencesKeys {
        const val PREFERENCE_STORE = "user-preferences"

        const val LAYOUT_VERTICAL = 1
        const val LAYOUT_GRID = 2
        val IS_GRID = intPreferencesKey("is_grid")

        val THEME = intPreferencesKey("theme")
        const val THEME_SYSTEM = 1
        const val THEME_LIGHT = 2
        const val THEME_DARK = 3

        val FILTER_IDS = stringSetPreferencesKey("filter_categories_ids")
    }
}


