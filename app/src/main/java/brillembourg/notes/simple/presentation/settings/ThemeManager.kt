package brillembourg.notes.simple.presentation.settings

interface ThemeManager {
    fun changeTheme(theme: ThemeMode?)
    val defaultTheme: ThemeMode
    val themeList: List<ThemeMode>
}