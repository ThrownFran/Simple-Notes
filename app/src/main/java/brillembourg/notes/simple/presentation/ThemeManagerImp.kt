package brillembourg.notes.simple.presentation

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import brillembourg.notes.simple.domain.models.ThemeMode
import brillembourg.notes.simple.domain.use_cases.theme.ThemeManager

class ThemeManagerImp : ThemeManager {
    override fun changeTheme(theme: ThemeMode?) {
        when (theme) {
            ThemeMode.System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            ThemeMode.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    //Only Android 10 supports follow system
    override val defaultTheme: ThemeMode
        get() =//Only Android 10 supports follow system
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThemeMode.System
            } else {
                ThemeMode.Light
            }

    //Only Android 10 supports follow system
    override val themeList: List<ThemeMode>
        get() {
            val list: MutableList<ThemeMode> = ArrayList()
            list.add(ThemeMode.Light)
            list.add(ThemeMode.Dark)
            //Only Android 10 supports follow system
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                list.add(ThemeMode.System)
            }
            return list
        }
}