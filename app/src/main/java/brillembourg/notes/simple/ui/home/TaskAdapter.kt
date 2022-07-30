package brillembourg.notes.simple.ui.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.Nullable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import java.util.*


class TaskAdapter(
    val recyclerView: RecyclerView,
    val onClick: (TaskPresentationModel) -> Unit,
    val onLongClick: (TaskPresentationModel) -> Unit,
    val onReorder: (List<TaskPresentationModel>) -> Unit
) : ListAdapter<TaskPresentationModel, TaskAdapter.ViewHolder>(DiffCallback) {

    var dragAndDrogList: List<TaskPresentationModel>? = null

    val itemTouchHelper by lazy { setupDragAndDropTouchHelper() }

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

    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            setupClickListeners()
            correctImageHeight()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener { click() }
            binding.root.setOnLongClickListener {
                clickLong()
                true
            }

            binding.root.setOnLongClickListener {
                startDrag()
                true
            }
        }

        private fun click() {
            onClick.invoke(getItem(adapterPosition))
        }

        private fun clickLong() {
            itemTouchHelper.attachToRecyclerView(null)
            onLongClick.invoke(getItem(adapterPosition))
        }

        private fun startDrag() {
            itemTouchHelper.attachToRecyclerView(recyclerView)
            itemTouchHelper.startDrag(this)
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

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (dragAndDrogList == null) {
            dragAndDrogList = currentList.toMutableList()
        }
        dragAndDrogList?.let { Collections.swap(it, fromPosition, toPosition) };
        Log.e("ERROR", "SWAPPING $fromPosition to $toPosition")
        Log.e("Current list", dragAndDrogList.toString())
    }

    private fun setupDragAndDropTouchHelper(): ItemTouchHelper {
        val itemTouchCallback =
            object : SimpleCallback(UP or DOWN or START or END, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val recyclerviewAdapter = recyclerView.adapter as TaskAdapter
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition
                    recyclerviewAdapter.moveItem(fromPosition, toPosition)
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
                    if (dragAndDrogList == null) return
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

                    dragAndDrogList?.let { onReorder.invoke(it) }
                    dragAndDrogList = null
                }

                override fun onSelectedChanged(
                    @Nullable viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    when (actionState) {
                        ACTION_STATE_DRAG -> {}                // the user is dragging an item and didn't lift their finger off yet
                        ACTION_STATE_SWIPE -> {}                   // the user is swiping an item and didn't lift their finger off yet
                        ACTION_STATE_IDLE -> {
                            // the user just dropped the item (after dragging it), and lift their finger off.
                            Log.e("ITEM TOUCH HELPER", "IDLE")
                        }
                    }
                }
            }
        return ItemTouchHelper(itemTouchCallback)
    }

    companion object {
        private val DiffCallback = setupDiffCallback()

        private fun setupDiffCallback() = object : DiffUtil.ItemCallback<TaskPresentationModel>() {
            override fun areItemsTheSame(
                oldItem: TaskPresentationModel,
                newItem: TaskPresentationModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: TaskPresentationModel,
                newItem: TaskPresentationModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

