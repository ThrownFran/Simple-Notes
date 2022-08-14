package brillembourg.notes.simple.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.extras.fromDpToPixel
import brillembourg.notes.simple.ui.models.TaskPresentationModel

class TaskAdapter(
    dragAndDropDirs: Int,
    val recyclerView: RecyclerView,
    val onClick: (TaskPresentationModel, View) -> Unit,
    val onSelection: () -> Unit,
    val onReorderSuccess: (List<TaskPresentationModel>) -> Unit,
    val onReorderCanceled: () -> Unit
) : ListAdapter<TaskPresentationModel, TaskAdapter.ViewHolder>(DiffCallback) {

    var dragAndDrogList: List<TaskPresentationModel>? = null
    var isDragging = false
    var tracker: SelectionTracker<Long>? = null
    var currentPosition: Int? = null

    var itemTouchHelper = setupDragAndDropTouchHelper(dragAndDropDirs)

    override fun submitList(list: List<TaskPresentationModel>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            setupClickListeners()
        }

        private fun setupClickListeners() {
            binding.taskCardview.setOnClickListener { click() }

            binding.taskCardview.setOnLongClickListener {
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
        }

        private fun longClickInNormalState() {
            startDrag()
            toggleItemSelection()
        }

        private fun click() {
            if (isSelectionVisible()) {
                clickInSelectionVisible()
                return
            }
            clickInNormalState()
        }

        private fun clickInNormalState() {
            onClick.invoke(getItem(adapterPosition), binding.root)
        }

        private fun clickInSelectionVisible() {
            toggleItemSelection()
        }

        fun bind(task: TaskPresentationModel) {
            binding.task = task
            bindTitle(task)
            bindContent(task)
            bindDate(task)
            bindSelection(task)
        }

        private fun bindDate(task: TaskPresentationModel) {
            binding.taskTextDate.text = task.dateInLocal
        }

        private fun bindContent(task: TaskPresentationModel) {
//            binding.taskTextContent.text = "${task.order}. ${task.content}"
            binding.taskTextContent.text = task.content
        }

        fun bindSelection(task: TaskPresentationModel) {
//            binding.taskCardview.isChecked = task.isSelected
            if (task.isSelected) {
                setBackgroundSelected()
            } else {
                setBackgroundTransparent()
            }
        }

        fun setBackgroundTransparent() {
            binding.taskCardview.isChecked = false
            binding.taskCardview.strokeWidth =
                1f.fromDpToPixel(binding.taskCardview.context).toInt()
        }

        fun setBackgroundSelected() {
            binding.taskCardview.isChecked = true
            binding.taskCardview.strokeWidth =
                3f.fromDpToPixel(binding.taskCardview.context).toInt()
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

        private fun isSelectionVisible(): Boolean =
            currentList.any { it.isSelected }

        private fun startDrag() {
            enableDragNDrop()
            itemTouchHelper.startDrag(this@ViewHolder)
        }

        private fun enableDragNDrop() {
            itemTouchHelper.attachToRecyclerView(recyclerView)
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

