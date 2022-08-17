package brillembourg.notes.simple.presentation.ui_utils

import android.content.res.Resources
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ListAdapter
import brillembourg.notes.simple.R
import brillembourg.notes.simple.presentation.home.NoteViewHolder
import brillembourg.notes.simple.presentation.models.TaskPresentationModel

fun setupContextualActionBar(
    toolbar: Toolbar,
    @MenuRes menuId: Int,
    currentActionMode: ActionMode?,
    adapter: ListAdapter<TaskPresentationModel, NoteViewHolder>,
    onActionClick: (menuId: Int) -> Boolean,
    onSetTitle: (selectedSize: Int) -> String,
    onDestroyMyActionMode: () -> Unit
): ActionMode? {
    val taskList = adapter.currentList
    val selectedList = taskList.filter { it.isSelected }

    if (selectedList.isEmpty()) {
        currentActionMode?.finish()
        return null
    }

    if (currentActionMode != null) {
        currentActionMode.title = onSetTitle.invoke(selectedList.size)
        return currentActionMode
    }


    val actionMode = toolbar.startActionMode(object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(menuId, menu)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            onSetTitle.invoke(selectedList.size)
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return onActionClick.invoke(item.itemId)
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {

            taskList.forEachIndexed { index, taskPresentationModel ->
                if (taskPresentationModel.isSelected) {
                    taskPresentationModel.isSelected = false
                    adapter.notifyItemChanged(index, taskPresentationModel)
                }
            }

            onDestroyMyActionMode.invoke()
        }
    })
    actionMode.title = onSetTitle.invoke(selectedList.size)
    return actionMode
}

fun getNoteSelectedTitle(resources: Resources, selectedSize: Int): String {
    val noteString =
        if (selectedSize > 1) resources.getString(R.string.notes) else resources.getString(R.string.note)
    return "$selectedSize ${noteString.lowercase()} ${
        resources.getString(R.string.selected).lowercase()
    })"
}