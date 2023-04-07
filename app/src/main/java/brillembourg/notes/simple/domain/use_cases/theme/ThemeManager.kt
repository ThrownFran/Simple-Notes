package brillembourg.notes.simple.domain.use_cases.theme

import brillembourg.notes.simple.domain.models.ThemeMode

interface ThemeManager {
    fun changeTheme(theme: ThemeMode?)
    val defaultTheme: ThemeMode
    val themeList: List<ThemeMode>
}
