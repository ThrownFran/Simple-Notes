package brillembourg.notes.simple.presentation.ui_utils.recycler_view

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.presentation.models.HasOrder
import java.util.Collections

interface Draggable<T : HasOrder> {

    fun startDrag(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        onGetCurrentList: () -> List<T>,
        onSubmitList: (noteList: (List<T>)?, submitSuccess: () -> Unit) -> Unit,
        onReorderSuccess: (List<T>) -> Unit,
        onReorderCanceled: () -> Unit
    )

    fun setDragDirections(recyclerView: RecyclerView, dragDirs: Int)
}

class ItemTouchDraggableImp<T : HasOrder>(
    recyclerView: RecyclerView,
    dragAndDropDirs: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN,
) : Draggable<T> {

    private var itemTouchHelper = setupDragAndDropTouchHelper(dragAndDropDirs)
        .also { it.attachToRecyclerView(recyclerView) }
    private var isDragging: Boolean = false
    private var dragAndDropList: List<T>? = null

    private val originalOrder: MutableList<Int> = ArrayList()

    //Adapter callbacks
    private var onGetCurrentList: (() -> List<T>)? = null
    private var onSubmitList: ((noteList: (List<T>)?, submitSuccess: () -> Unit) -> Unit)? = null
    private var onReorderSuccess: ((List<T>) -> Unit)? = null
    private var onReorderCanceled: (() -> Unit)? = null

    override fun setDragDirections(recyclerView: RecyclerView, dragDirs: Int) {
        itemTouchHelper = setupDragAndDropTouchHelper(dragDirs).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    override fun startDrag(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        onGetCurrentList: () -> List<T>,
        onSubmitList: (noteList: (List<T>)?, submitSuccess: () -> Unit) -> Unit,
        onReorderSuccess: (List<T>) -> Unit,
        onReorderCanceled: () -> Unit
    ) {
        this.onGetCurrentList = onGetCurrentList
        this.onSubmitList = onSubmitList
        this.onReorderSuccess = onReorderSuccess
        this.onReorderCanceled = onReorderCanceled
        itemTouchHelper.startDrag(viewHolder)
    }

    private fun setupDragAndDropTouchHelper(dragAndDropDirs: Int): ItemTouchHelper {
        val itemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(
                dragAndDropDirs,
                0
            ) {

                override fun isLongPressDragEnabled(): Boolean {
                    return false
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {

                    val listAdapter: ListAdapter<*, *> = when (val adapter = recyclerView.adapter) {
                        is ConcatAdapter -> adapter.adapters.find { it is ListAdapter<*, *> } as ListAdapter<*, *>
                        is ListAdapter<*, *> -> adapter
                        else -> throw IllegalArgumentException("Adapter $adapter is not a ListAdapter")
                    }

                    val fromPosition = viewHolder.bindingAdapterPosition
                    val toPosition = target.bindingAdapterPosition
                    isDragging = true

                    dragAndDropList = getListToSwap()
                    setOrderMap()

                    //0:0 , 1:1, 2:2, 3:3

                    //2:2, 1:1, 0:0, 3:3

                    swapListIndexPositions(dragAndDropList!!, fromPosition, toPosition)
                    listAdapter.notifyItemMoved(fromPosition, toPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if (!isDragging) {
                        return
                    }

                    isDragging = false

                    if (dragAndDropList == null) {
                        onReorderCanceled?.invoke()
                        return
                    }

                    changeOrderInListWithIndexPositions()
                    notifyListChanged(recyclerView)

                    dragAndDropList?.let { onReorderSuccess?.invoke(it) }
                    dragAndDropList = null
                }
            }

        return ItemTouchHelper(itemTouchCallback)
    }

    private fun setOrderMap() {
        //Save note order in from each index. So we can swap orders after dragging.
        dragAndDropList?.reversed()?.forEachIndexed { index, task ->
            originalOrder.add(task.order)
        }
    }

    private fun getListToSwap(): List<T> {
        if (dragAndDropList == null) {
            dragAndDropList = onGetCurrentList?.invoke()?.toMutableList()
        }
        return dragAndDropList!!
    }

    private fun swapListIndexPositions(
        noteList: List<T>,
        fromPosition: Int,
        toPosition: Int
    ) {
        noteList.let {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(it, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(it, i, i - 1)
                }
            }
        }
    }

    private fun notifyListChanged(recyclerView: RecyclerView) {
        recyclerView.itemAnimator = null
        val state = recyclerView.layoutManager?.onSaveInstanceState()

        onSubmitList?.invoke(null) {}
        onSubmitList?.invoke(dragAndDropList) {
            recyclerView.post {
                recyclerView.itemAnimator = DefaultItemAnimator()
            }
        }
        recyclerView.layoutManager?.onRestoreInstanceState(state)
    }

    private fun changeOrderInListWithIndexPositions() {

        dragAndDropList?.reversed()?.forEachIndexed { index, taskPresentationModel ->
            val newOrder = originalOrder[index]
            taskPresentationModel.order = newOrder
        }
    }

}
