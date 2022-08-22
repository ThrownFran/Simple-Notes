package brillembourg.notes.simple.presentation.ui_utils

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.presentation.home.NoteAdapter
import java.util.*

fun NoteAdapter.setupDragAndDropTouchHelper(dragAndDropDirs: Int): ItemTouchHelper {
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

                if (dragAndDrogList == null) {
                    dragAndDrogList = currentList.toMutableList()
                }
                dragAndDrogList?.let {
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


                recyclerviewAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                if (!isDragging) {
                    return
                }

                isDragging = false

                if (dragAndDrogList == null) {
                    onReorderCanceled.invoke()
                    return
                }

                dragAndDrogList?.forEachIndexed { index, taskPresentationModel ->
                    taskPresentationModel.order = dragAndDrogList!!.size - index - 1
//                    taskPresentationModel.order = index + 1
                    taskPresentationModel.isSelected = false
                }

                recyclerView.itemAnimator = null
                val state = recyclerView.layoutManager?.onSaveInstanceState()
                submitList(null)
                submitList(dragAndDrogList) {
                    recyclerView.post {
                        recyclerView.itemAnimator = DefaultItemAnimator()
                    }
                }
                recyclerView.layoutManager?.onRestoreInstanceState(state)

                dragAndDrogList?.let {
                    onReorderSuccess.invoke(it)
                }
                dragAndDrogList = null
            }
        }

    return ItemTouchHelper(itemTouchCallback)
}
