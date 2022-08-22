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
import brillembourg.notes.simple.presentation.models.NotePresentationModel

fun setupContextualActionBar(
    toolbar: Toolbar,
    @MenuRes menuId: Int,
    currentActionMode: ActionMode?,
    adapter: ListAdapter<NotePresentationModel, NoteViewHolder>,
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
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(menuId, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            onSetTitle.invoke(selectedList.size)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return onActionClick.invoke(item.itemId)
        }

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