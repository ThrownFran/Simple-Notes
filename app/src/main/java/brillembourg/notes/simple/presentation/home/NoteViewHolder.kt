package brillembourg.notes.simple.presentation.home

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.custom_views.fromDpToPixel
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.Selectable
import brillembourg.notes.simple.presentation.ui_utils.SelectableImp
import com.google.android.material.chip.Chip

class NoteViewHolder(
    private val getCurrentList: () -> List<NotePresentationModel>,
    private val binding: ItemNoteBinding,
    onClick: ((NotePresentationModel) -> Unit)? = null,
    private val onSelected: (() -> Unit)? = null,
    private val onReadyToDrag: ((NoteViewHolder) -> Unit)? = null
) : RecyclerView.ViewHolder(binding.root),
    Selectable<NotePresentationModel> by SelectableImp(getCurrentList) {

    init {
        setupClickListeners()
        setupSelection(
            bindSelection = { note -> bindSelection(note) },
            onSelected = { onSelected?.invoke() },
            onClickWithNoSelection = onClick,
            onReadyToDrag = { onReadyToDrag?.invoke(this) }
        )
    }

    private fun setupClickListeners() {

        binding.taskCardview.setOnClickListener {
            onItemClick(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
        }

        binding.taskCardview.setOnLongClickListener {
            onItemSelection(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
            true
        }
    }

    fun bind(task: NotePresentationModel) {
        binding.task = task
        bindTitle(task)
        bindContent(task)
        bindDate(task)
        bindSelection(task)
        bindCategories(task)
    }

    private fun bindCategories(model: NotePresentationModel) {
        if (model.categories.isEmpty()) binding.taskChipgroupCategories.removeAllViews()

        val categories = model.categories
        binding.taskChipgroupCategories.removeAllViews()
        categories.forEach {
            val chip = Chip(itemView.context)
            chip.text = it.name
            chip.id = it.id.toInt()
            binding.taskChipgroupCategories.addView(chip)
        }
    }

    private fun bindDate(task: NotePresentationModel) {
        binding.taskTextDate.text = task.dateInLocal
    }

    fun bindContent(task: NotePresentationModel) {
//            binding.taskTextContent.text = "${task.order}. ${task.content}"
        binding.taskTextContent.apply {
            text = task.content
            isVisible = task.content.isNotEmpty()
        }
    }

    fun bindSelection(task: NotePresentationModel) {
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

    fun bindTitle(task: NotePresentationModel) {
        with(binding.taskTextTitle) {
            isVisible = !task.title.isNullOrEmpty()
            text = task.title
        }
    }


}