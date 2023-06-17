package brillembourg.notes.simple.presentation.settings

import android.content.res.Resources

interface IsOption {
    fun getName(resources: Resources): String
    fun getValue(): String
}