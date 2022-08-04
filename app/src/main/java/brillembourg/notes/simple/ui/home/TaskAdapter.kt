package brillembourg.notes.simple.ui.home

import android.content.Context
import android.view.*
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel


class TaskAdapter(
    val menuInflater: MenuInflater,
    val recyclerView: RecyclerView,
    val onClick: (TaskPresentationModel) -> Unit,
    val onLongClick: (TaskPresentationModel) -> Unit,
    val onReorder: (List<TaskPresentationModel>) -> Unit
) : ListAdapter<TaskPresentationModel, TaskAdapter.ViewHolder>(DiffCallback) {

    var dragAndDrogList: List<TaskPresentationModel>? = null
    var tracker: SelectionTracker<Long>? = null
    var currentPosition: Int? = null

    val itemTouchHelper by lazy { setupDragAndDropTouchHelper() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener {

        init {
            setupClickListeners()
            correctImageHeight()
//            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            p1: View?,
            p2: ContextMenu.ContextMenuInfo?
        ) {
            menuInflater.inflate(R.menu.context_menu, menu)
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener { click() }
//            binding.root.setOnLongClickListener {
//                clickLong()
//                true
//            }

            binding.root.setOnLongClickListener {
                currentPosition = adapterPosition

                if (!isSelectionVisible()) {
                    startDrag()
                }

                toggleItem()

                if (!isSelectionVisible()) {
                    itemTouchHelper.attachToRecyclerView(recyclerView)
                }
                true
            }
        }

        private fun toggleItem() {
            getItem(adapterPosition)?.let {
                it.isSelected = !it.isSelected
                setSelection(it)
            }
        }

        private fun click() {

            if (isSelectionVisible()) {
                toggleItem()

                if (isSelectionVisible()) {
                    itemTouchHelper.attachToRecyclerView(null)
                } else {
                    itemTouchHelper.attachToRecyclerView(recyclerView)
                }
                return
            }

            itemTouchHelper.attachToRecyclerView(recyclerView)
            onClick.invoke(getItem(adapterPosition))
        }

        private fun isSelectionVisible(): Boolean =
            currentList.any { it.isSelected }

//        private fun clickLong() {
//            itemTouchHelper.attachToRecyclerView(null)
//            onLongClick.invoke(getItem(adapterPosition))
//        }

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
            setSelection(task)
        }

        private fun setSelection(task: TaskPresentationModel) {
            if (task.isSelected) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.black)
                )
            } else {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
            }
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

