package brillembourg.notes.simple.presentation.home.renderers

import android.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.presentation.trash.SelectionModeActive
import brillembourg.notes.simple.presentation.ui_utils.getNoteSelectedTitle
import brillembourg.notes.simple.presentation.ui_utils.setupContextualActionBar

class SelectionRenderer(
    private val toolbar: Toolbar,
    private val menuId: Int,
    private val recyclerView: RecyclerView,
    private val onActionClick: (menuId: Int) -> Boolean,
    private val onSelectionDismissed: () -> Unit,
    private val onSetTitle: ((selectedSize: Int) -> String)? = null
) {

    private var actionMode: ActionMode? = null

    fun render(selectionModeActive: SelectionModeActive) {
        if (selectionModeActive.isActive.not()) {
            actionMode?.finish()
            actionMode = null
            return
        }

        launchContextualActionBar(selectionModeActive.size)
    }

    private fun launchContextualActionBar(sizeSelected: Int) {
        actionMode = setupContextualActionBar(
            size = sizeSelected,
            toolbar = toolbar,
            menuId = menuId,
            currentActionMode = actionMode,
            onActionClick = { onActionClick(it) },
            onSetTitle = { selectedSize: Int ->
                onSetTitle?.invoke(selectedSize) ?: getNoteSelectedTitle(
                    resources = recyclerView.resources,
                    selectedSize = selectedSize
                )
            },
            onDestroyMyActionMode = { onSelectionDismissed() }
        )
    }
}