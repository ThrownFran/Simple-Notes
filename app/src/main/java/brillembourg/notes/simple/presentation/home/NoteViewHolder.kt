package brillembourg.notes.simple.presentation.home

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.extras.fromDpToPixel
import brillembourg.notes.simple.presentation.models.TaskPresentationModel

class NoteViewHolder(
    private val getCurrentList: () -> List<TaskPresentationModel>,
    private val binding: ItemNoteBinding,
    private val onClick: ((TaskPresentationModel, View) -> Unit)? = null,
    private val onSelected: (() -> Unit)? = null,
    private val onReadyToDrag: ((NoteViewHolder) -> Unit)? = null

) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.taskCardview.setOnClickListener { click() }

        binding.taskCardview.setOnLongClickListener {
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
        onClick?.invoke(getCurrentList()[adapterPosition], binding.root)
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

    fun bindContent(task: TaskPresentationModel) {
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

    fun bindTitle(task: TaskPresentationModel) {
        with(binding.taskTextTitle) {
            isVisible = !task.title.isNullOrEmpty()
            text = task.title
        }
    }

    private fun isSelectionNotVisible() = !isSelectionVisible()

    private fun toggleItemSelection() {
        getCurrentList()[adapterPosition]?.let {
            it.isSelected = !it.isSelected
            bindSelection(it)
            onSelected?.invoke()
        }
    }

    private fun isSelectionVisible(): Boolean =
        getCurrentList().any { it.isSelected }

    private fun startDrag() {
        onReadyToDrag?.invoke(this)
    }

    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.getResources().getDisplayMetrics().density
    }

}