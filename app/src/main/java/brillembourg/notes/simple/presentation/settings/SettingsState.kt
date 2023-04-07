package brillembourg.notes.simple.presentation.settings

sealed class SettingsState {

    data class Success(
        val themeMode: ThemeMode,
    ) : SettingsState()

    object Loading : SettingsState()
    data class ShowThemesView(val list: List<ThemeMode>, val current: ThemeMode) : SettingsState()

}

enum class NavDestination {
    About, Rate, SourceLicense
}