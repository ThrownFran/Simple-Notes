package brillembourg.notes.simple.ui.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import java.util.*

class TaskAdapter(
    val onClick: (TaskPresentationModel) -> Unit,
    val onLongClick: (TaskPresentationModel) -> Unit,
    val onReorder: (List<TaskPresentationModel>) -> Unit
) : ListAdapter<TaskPresentationModel, TaskAdapter.ViewHolder>(DiffCallback) {

    var dragAndDrogList: List<TaskPresentationModel>? = null

    val itemTouchHelper by lazy {
        val itemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val recyclerviewAdapter = recyclerView.adapter as TaskAdapter
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition
                    recyclerviewAdapter.moveItem(fromPosition, toPosition)
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
                    val recyclerviewAdapter = recyclerView.adapter as TaskAdapter
                    dragAndDrogList?.forEachIndexed { index, taskPresentationModel ->
                        //            taskPresentationModel.order = size - index
                        taskPresentationModel.order = index + 1
                    }

                    dragAndDrogList?.let { onReorder.invoke(it) }
//                    submitList(ArrayList())
//                    submitList(dragAndDrogList)
//                    dragAndDrogList = null
//                    recyclerView.itemAnimator = null
//                    recyclerviewAdapter.notifyDataSetChanged()
                }
            }
        ItemTouchHelper(itemTouchCallback)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TaskPresentationModel>() {
            override fun areItemsTheSame(
                oldItem: TaskPresentationModel,
                newItem: TaskPresentationModel
            ): Boolean {
                return oldItem.id == newItem.id
//                        && oldItem.order == newItem.order
            }

            override fun areContentsTheSame(
                oldItem: TaskPresentationModel,
                newItem: TaskPresentationModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (dragAndDrogList == null) {
            dragAndDrogList = currentList.toMutableList()
        }
//        val list = currentList.toMutableList()
//        val fromItem = list[fromPosition]
//        list.removeAt(fromPosition)
//        list.add(toPosition, fromItem)
//        if (toPosition < fromPosition) {
//            list.add(toPosition + 1 , fromItem)
//        } else {
//            list.add(toPosition - 1, fromItem)
//        }
        dragAndDrogList?.let { Collections.swap(it, fromPosition, toPosition) };
        Log.e("ERROR", "SWAPPING $fromPosition to $toPosition")
        Log.e("Current list", dragAndDrogList.toString())
//        val size = list.size

//        dragAndDrogList = list
    }


    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            setupClickListeners()
            correctImageHeight()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener { onClick.invoke(getItem(adapterPosition)) }
            binding.root.setOnLongClickListener {
                itemTouchHelper.startDrag(this)
//                onLongClick.invoke(getItem(adapterPosition))
                true
            }
        }

        private fun correctImageHeight() {
            val observer: ViewTreeObserver = binding.taskContraint.viewTreeObserver
            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.taskContraint.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    //Your code
                    val height = binding.taskContraint.measuredHeight
                    binding.taskImageBackground.layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, height +
                                    pxFromDp(binding.taskContraint.context, 4f).toInt()
                        )
                }
            })
        }

        fun bind(task: TaskPresentationModel) {
            bindTitle(task)
            binding.taskTextContent.text = "${task.order}. ${task.content}"
            binding.taskTextDate.text = task.dateInLocal


        }

        private fun bindTitle(task: TaskPresentationModel) {
            with(binding.taskTextTitle) {
                isVisible = !task.title.isNullOrEmpty()
                text = task.title
            }
        }

        fun dpFromPx(context: Context, px: Float): Float {
            return px / context.getResources().getDisplayMetrics().density
        }

        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.getResources().getDisplayMetrics().density
        }

    }
}

