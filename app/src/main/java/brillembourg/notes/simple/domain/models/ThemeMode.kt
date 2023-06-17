package brillembourg.notes.simple.domain.models

import android.content.res.Resources
import brillembourg.notes.simple.R
import brillembourg.notes.simple.presentation.settings.IsOption

enum class ThemeMode(var type: String) : IsOption {
    Light("0"), Dark("1"), System("2");

    override fun getValue(): String {
        return type
    }

    override fun getName(resources: Resources): String {
        return when (this) {
            Light -> resources.getString(R.string.settings_theme_light)
            Dark -> resources.getString(R.string.settings_theme_dark)
            System -> resources.getString(R.string.settings_theme_system)
        }
    }

    companion object {
        fun getThemeFromType(type: Int): ThemeMode {
            return when (type.toString()) {
                System.type -> {
                    System
                }

                Light.type -> {
                    Light
                }

                Dark.type -> {
                    Dark
                }

                else -> {
                    throw IllegalArgumentException("Type: $type is not a valid argument to create ThemeType")
                }
            }
        }
    }
}
