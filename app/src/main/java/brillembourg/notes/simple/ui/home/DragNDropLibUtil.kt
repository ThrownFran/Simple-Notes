package brillembourg.notes.simple.ui.home

import android.util.Log
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

fun TaskAdapter.setupDragAndDropTouchHelper(): ItemTouchHelper {
    val itemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val recyclerviewAdapter = recyclerView.adapter as TaskAdapter
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

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


//                    Collections.swap(it, fromPosition, toPosition)
                };


                Log.e("Current list", dragAndDrogList.toString())

                //                    submitList(dragAndDrogList)
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
                if (dragAndDrogList == null) {
                    onReorderCanceled.invoke()
                    return
                }

                dragAndDrogList?.forEachIndexed { index, taskPresentationModel ->
                    //            taskPresentationModel.order = size - index
                    taskPresentationModel.order = index + 1
                }

                recyclerView.itemAnimator = null
                submitList(null)
                submitList(dragAndDrogList) {
                    recyclerView.post {
                        recyclerView.itemAnimator = DefaultItemAnimator()
                    }
                }

                dragAndDrogList?.let { onReorderSuccess.invoke(it) }
                dragAndDrogList = null
            }

            override fun onSelectedChanged(
                @Nullable viewHolder: RecyclerView.ViewHolder?,
                actionState: Int
            ) {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {}                // the user is dragging an item and didn't lift their finger off yet
                    ItemTouchHelper.ACTION_STATE_SWIPE -> {}                   // the user is swiping an item and didn't lift their finger off yet
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        // the user just dropped the item (after dragging it), and lift their finger off.
                        Log.e("ITEM TOUCH HELPER", "IDLE")
                    }
                }
            }
        }
    return ItemTouchHelper(itemTouchCallback)
}
