package brillembourg.notes.simple.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.models.ThemeMode
import brillembourg.notes.simple.domain.models.UserPreferences
import brillembourg.notes.simple.domain.use_cases.theme.GetThemeUseCase
import brillembourg.notes.simple.domain.use_cases.user.GetUserPrefUseCase
import brillembourg.notes.simple.domain.use_cases.user.SaveUserPrefUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val messageManager: MessageManager,
    private val getUserPrefUseCase: GetUserPrefUseCase,
    private val saveUserPrefUseCase: SaveUserPrefUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private lateinit var currentTheme: ThemeMode
    private lateinit var themeList: List<ThemeMode>

    private var job: Job? = null
    private val errorHandler =
        CoroutineExceptionHandler { _, throwable -> onError(throwable) }
    val state = MutableStateFlow<SettingsState>(SettingsState.Loading)

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            getUserPrefUseCase(GetUserPrefUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            currentTheme = result.data.preferences.theme
                            handleGetSettings()
                        }
                        is Resource.Error -> messageManager.showError(result.exception)
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    private fun handleGetSettings() {

        job = CoroutineScope(Dispatchers.Default + errorHandler).launch {

            //Theme
            val result = getThemeUseCase()
            themeList = result.themeList

            state.update { SettingsState.Success(currentTheme) }
        }

    }

    private fun onError(throwable: Throwable) {
        messageManager.showMessage(getMessageFromError(throwable as Exception))
    }

    fun saveAndSetTheme(themeSelected: ThemeMode) {
        job = CoroutineScope(Dispatchers.Default + errorHandler).launch {
            saveUserPrefUseCase.invoke(SaveUserPrefUseCase.Params(UserPreferences(_theme = themeSelected)))
            state.update { SettingsState.Success(themeSelected) }
        }
    }

    fun clickShowTheme() {
        state.update { SettingsState.ShowThemesView(themeList, currentTheme) }
    }

}