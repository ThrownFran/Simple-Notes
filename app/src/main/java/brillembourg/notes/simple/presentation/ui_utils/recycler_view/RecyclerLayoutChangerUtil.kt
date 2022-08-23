package brillembourg.notes.simple.presentation.ui_utils.recycler_view

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import brillembourg.notes.simple.domain.models.NoteLayout

fun NoteLayout.toLayoutType(): LayoutType {
    return when (this) {
        NoteLayout.Vertical -> LayoutType.LinearVertical
        NoteLayout.Grid -> LayoutType.Staggered
    }
}

enum class LayoutType {
    LinearVertical, Staggered
}

fun buildLayoutManager(context: Context, layoutType: LayoutType): RecyclerView.LayoutManager {
    return if (layoutType == LayoutType.Staggered) buildStaggeredManager(context) else buildVerticalManager(
        context
    )
}

fun buildVerticalManager(context: Context) = LinearLayoutManager(context)

fun buildStaggeredManager(context: Context) =
    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL).also {
        it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
    }

fun changeLayout(
    recyclerView: RecyclerView,
    type: LayoutType,
    currentList: Any?
) {
    recyclerView.apply {

        val isLayoutChangeNeeded: Boolean = when (type) {
            LayoutType.LinearVertical -> layoutManager is StaggeredGridLayoutManager
            LayoutType.Staggered -> layoutManager is LinearLayoutManager
        }

        if (!isLayoutChangeNeeded) return@apply

        layoutManager = when (type) {
            LayoutType.LinearVertical -> buildVerticalManager(recyclerView.context)
            LayoutType.Staggered -> buildStaggeredManager(context)
        }

        adapter?.notifyItemRangeChanged(
            0, adapter?.itemCount ?: 0,
            currentList
        )
    }
}

fun getDragDirs(layoutType: LayoutType) = when (layoutType) {
    LayoutType.Staggered -> {
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
    }
    LayoutType.LinearVertical -> {
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }
}