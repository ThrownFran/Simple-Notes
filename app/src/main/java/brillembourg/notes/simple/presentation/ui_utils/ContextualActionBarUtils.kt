package brillembourg.notes.simple.presentation.ui_utils

import android.content.res.Resources
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import brillembourg.notes.simple.R

inline fun setupContextualActionBar(
    size: Int,
    toolbar: Toolbar,
    @MenuRes menuId: Int,
    currentActionMode: ActionMode?,
    crossinline onActionClick: (menuId: Int) -> Boolean,
    crossinline onSetTitle: (selectedSize: Int) -> String,
    crossinline onDestroyMyActionMode: () -> Unit
): ActionMode? {

    if (size <= 0) {
        currentActionMode?.finish()
        return null
    }

    if (currentActionMode != null) {
        currentActionMode.title = onSetTitle.invoke(size)
        return currentActionMode
    }

    val actionMode = toolbar.startActionMode(object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(menuId, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            onSetTitle.invoke(size)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return onActionClick.invoke(item.itemId)
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            onDestroyMyActionMode.invoke()
        }
    })
    actionMode.title = onSetTitle.invoke(size)
    return actionMode
}

fun getNoteSelectedTitle(resources: Resources, selectedSize: Int): String {
    val noteString =
        if (selectedSize > 1) resources.getString(R.string.notes) else resources.getString(R.string.note)
    return "$selectedSize ${noteString.lowercase()} ${
        resources.getString(R.string.selected).lowercase()
    }"
}

fun getCategoriesSelectedTitle(resources: Resources, selectedSize: Int): String {
    val noteString =
        if (selectedSize > 1) resources.getString(R.string.categories) else resources.getString(R.string.category)
    return "$selectedSize ${noteString.lowercase()} ${
        resources.getString(R.string.selected).lowercase()
    }"
}