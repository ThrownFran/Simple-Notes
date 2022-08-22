package brillembourg.notes.simple.presentation.ui_utils

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.presentation.home.NoteAdapter
import brillembourg.notes.simple.presentation.home.NoteViewHolder
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import java.util.*

interface Draggable {

    fun startDrag(
        recyclerView: RecyclerView,
        viewHolder: NoteViewHolder,
        onGetCurrentList: () -> List<NotePresentationModel>,
        onSubmitList: (noteList: (List<NotePresentationModel>)?, submitSuccess: () -> Unit) -> Unit,
        onReorderSuccess: (List<NotePresentationModel>) -> Unit,
        onReorderCanceled: () -> Unit
    )

    fun changeDragDirections(recyclerView: RecyclerView, dragDirs: Int)
}

class ItemTouchDraggableImp(
    dragAndDropDirs: Int,
) : Draggable {

    var itemTouchHelper = setupDragAndDropTouchHelper(dragAndDropDirs)
    private var isDragging: Boolean = false
    private var dragAndDropList: List<NotePresentationModel>? = null

    //Adapter callbacks
    private var onGetCurrentList: (() -> List<NotePresentationModel>)? = null
    private var onSubmitList: ((noteList: (List<NotePresentationModel>)?, submitSuccess: () -> Unit) -> Unit)? =
        null
    private var onReorderSuccess: ((List<NotePresentationModel>) -> Unit)? = null
    private var onReorderCanceled: (() -> Unit)? = null

    override fun changeDragDirections(recyclerView: RecyclerView, dragDirs: Int) {
        itemTouchHelper = setupDragAndDropTouchHelper(dragDirs).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    override fun startDrag(
        recyclerView: RecyclerView,
        viewHolder: NoteViewHolder,
        onGetCurrentList: () -> List<NotePresentationModel>,
        onSubmitList: (noteList: (List<NotePresentationModel>)?, submitSuccess: () -> Unit) -> Unit,
        onReorderSuccess: (List<NotePresentationModel>) -> Unit,
        onReorderCanceled: () -> Unit
    ) {
        this.onGetCurrentList = onGetCurrentList
        this.onSubmitList = onSubmitList
        this.onReorderSuccess = onReorderSuccess
        this.onReorderCanceled = onReorderCanceled
        itemTouchHelper.attachToRecyclerView(recyclerView)
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

                    val recyclerviewAdapter = recyclerView.adapter as NoteAdapter
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition
                    isDragging = true

                    dragAndDropList = getListToSwap()
                    swapListIndexPositions(dragAndDropList!!, fromPosition, toPosition)
                    recyclerviewAdapter.notifyItemMoved(fromPosition, toPosition)
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

    private fun getListToSwap(): List<NotePresentationModel> {
        if (dragAndDropList == null) {
            dragAndDropList = onGetCurrentList?.invoke()?.toMutableList()
        }
        return dragAndDropList!!
    }

    private fun swapListIndexPositions(
        noteList: List<NotePresentationModel>,
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
        dragAndDropList?.forEachIndexed { index, taskPresentationModel ->
            taskPresentationModel.order = dragAndDropList!!.size - index - 1
            //                    taskPresentationModel.order = index + 1
            taskPresentationModel.isSelected = false
        }
    }

}





