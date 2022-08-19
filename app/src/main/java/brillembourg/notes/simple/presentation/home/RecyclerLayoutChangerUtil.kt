package brillembourg.notes.simple.presentation.home

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import brillembourg.notes.simple.presentation.models.TaskPresentationModel

enum class LayoutType {
    Vertical, Grid
}

fun buildLayoutManager(layoutType: LayoutType): RecyclerView.LayoutManager {
    return if (layoutType == LayoutType.Grid) buildStaggeredManager() else buildVerticalManager()
}

fun buildVerticalManager() = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL).also {
    it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
}

fun buildStaggeredManager() =
    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL).also {
        it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
    }

fun changeLayout(
    recyclerView: RecyclerView,
    type: LayoutType,
    currentList: List<TaskPresentationModel>
) {
    recyclerView.apply {
        val spanCount = when (type) {
            LayoutType.Grid -> 2
            LayoutType.Vertical -> 1
        }

        (layoutManager as StaggeredGridLayoutManager).spanCount = spanCount
        adapter?.notifyItemRangeChanged(
            0, adapter?.itemCount ?: 0,
            currentList
        )
    }
}

fun getDragDirs(layoutType: LayoutType) = when (layoutType) {
    LayoutType.Grid -> {
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
    }
    LayoutType.Vertical -> {
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }
}