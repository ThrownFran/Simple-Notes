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
import brillembourg.notes.simple.ui.extras.fromDpToPixel
import brillembourg.notes.simple.ui.models.TaskPresentationModel


class TaskAdapter(
    val menuInflater: MenuInflater,
    val recyclerView: RecyclerView,
    val onClick: (TaskPresentationModel) -> Unit,
    val onSelection: () -> Unit,
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
//            correctImageHeight()
//            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            p1: View?,
            p2: ContextMenu.ContextMenuInfo?
        ) {
            menuInflater.inflate(R.menu.menu_context, menu)
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener { click() }

            binding.root.setOnLongClickListener {
                currentPosition = adapterPosition

                if (isSelectionNotVisible()) {
                    longClickInNormalState()
                } else {
                    longClickInSelectionVisible()
                }
                true
            }
        }

        private fun longClickInSelectionVisible() {
            toggleItemSelection()
            if (isSelectionNotVisible()) {
                enableDragNDrop()
            }
        }

        private fun longClickInNormalState() {
            startDrag()
            toggleItemSelection()

            if (isSelectionNotVisible()) {
                enableDragNDrop()
            }
        }

        private fun click() {
            if (isSelectionVisible()) {
                clickInSelectionVisible()
                return
            }
            clickInNormalState()
        }

        private fun clickInNormalState() {
            enableDragNDrop()
            onClick.invoke(getItem(adapterPosition))
        }

        private fun clickInSelectionVisible() {
            toggleItemSelection()
            if (isSelectionVisible()) disableDragNDrop() else enableDragNDrop()
        }

        fun bind(task: TaskPresentationModel) {
            binding.taskImageBackground.isVisible = false
            bindTitle(task)
            bindContent(task)
            bindDate(task)
            bindSelection(task)
            correctImageHeight()
        }

        private fun bindDate(task: TaskPresentationModel) {
            binding.taskTextDate.text = task.dateInLocal
        }

        private fun bindContent(task: TaskPresentationModel) {
//            binding.taskTextContent.text = "${task.order}. ${task.content}"
            binding.taskTextContent.text = task.content
        }

        private fun bindSelection(task: TaskPresentationModel) {
            if (task.isSelected) {
//                binding.taskContraintExternal.setBackgroundColor(com.google.android.material.R.attr.colorSecondary)
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.teal_200)
                )
            } else {
//                binding.taskContraintExternal.setBackgroundColor(0)
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

        private fun isSelectionNotVisible() = !isSelectionVisible()

        private fun toggleItemSelection() {
            getItem(adapterPosition)?.let {
                it.isSelected = !it.isSelected
                bindSelection(it)
                onSelection.invoke()
            }
        }

        private fun disableDragNDrop() {
            itemTouchHelper.attachToRecyclerView(null)
        }

        private fun isSelectionVisible(): Boolean =
            currentList.any { it.isSelected }

        private fun startDrag() {
            enableDragNDrop()
            itemTouchHelper.startDrag(this)
        }

        private fun enableDragNDrop() {
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }


        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.getResources().getDisplayMetrics().density
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
                                    4f.fromDpToPixel(context = itemView.context).toInt()
                        )
                    binding.taskImageBackground.isVisible = true
//                    itemView.invalidate()
                }
            })
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

