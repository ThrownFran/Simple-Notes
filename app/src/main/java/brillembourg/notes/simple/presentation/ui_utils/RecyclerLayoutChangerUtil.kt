package brillembourg.notes.simple.presentation.ui_utils

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import brillembourg.notes.simple.presentation.models.TaskPresentationModel

enum class LayoutType {
    Vertical, Grid
}

fun buildLayoutManager(context: Context, layoutType: LayoutType): RecyclerView.LayoutManager {
    return if (layoutType == LayoutType.Grid) buildStaggeredManager(context) else buildVerticalManager(
        context
    )
}

fun buildVerticalManager(context: Context) = LinearLayoutManager(context)

//fun buildVerticalManager(context: Context) = StaggeredGridLayoutManager(1,RecyclerView.VERTICAL).also {
//    it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
//}

fun buildStaggeredManager(context: Context) =
    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL).also {
        it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
    }

fun changeLayout(
    recyclerView: RecyclerView,
    type: LayoutType,
    currentList: List<TaskPresentationModel>
) {
    recyclerView.apply {

        if (type == LayoutType.Grid) {
            val spanCount = when (type) {
                LayoutType.Grid -> 2
                LayoutType.Vertical -> 1
            }

            layoutManager = buildStaggeredManager(context)
//            (layoutManager as StaggeredGridLayoutManager).spanCount = spanCount
        } else {
            layoutManager = buildVerticalManager(recyclerView.context)
        }
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