package brillembourg.notes.simple.presentation.home

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import brillembourg.notes.simple.presentation.custom_views.fromDpToPixel
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.Selectable
import brillembourg.notes.simple.presentation.ui_utils.SelectableImp
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class NoteViewHolder(
    private val getCurrentList: () -> List<NotePresentationModel>,
    private val binding: ItemNoteBinding,
    onClick: ((NotePresentationModel) -> Unit)? = null,
    private val onSelected: ((isSelected: Boolean, id: Long) -> Unit)? = null,
    private val onReadyToDrag: ((NoteViewHolder) -> Unit)? = null
) : RecyclerView.ViewHolder(binding.root),
    Selectable<NotePresentationModel> by SelectableImp(getCurrentList) {

    init {
        setupClickListeners()
        setupSelection(
            bindSelection = ::bindSelection,
            onSelected = { isSelected, id -> onSelected?.invoke(isSelected, id) },
            onClickWithNoSelection = onClick,
            onReadyToDrag = { onReadyToDrag?.invoke(this) }
        )
    }

    private fun setupClickListeners() {

        binding.taskCardview.setOnClickListener {
            setClick()
        }

        binding.taskCardview.setOnLongClickListener {
            setLongClick()
            true
        }
    }

    private fun setLongClick() {
        onItemSelection(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
    }

    private fun setClick() {
        onItemClick(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
    }

    fun bind(task: NotePresentationModel) {
        binding.task = task
        bindTitle(task)
        bindContent(task)
        bindDate(task)
        bindSelection(task)
        bindCategories(task)
    }

    private fun bindCategories(task: NotePresentationModel) {
        val categories = task.categories.toDiplayOrder()

        binding.taskRecyclerCategories.apply {
            layoutManager = FlexboxLayoutManager(context)
                .apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                    flexWrap = FlexWrap.WRAP
                }
            isNestedScrollingEnabled = false
            isClickable = false
            adapter = CategoryChipColorSecondaryAdapter(
                onClick = { setClick() },
                onLongClick = { setLongClick() }
            ).apply { submitList(categories) }
        }
    }

    private fun bindDate(task: NotePresentationModel) {
        binding.taskTextDate.text = task.dateInLocal
    }

    private fun bindContent(task: NotePresentationModel) {
//            binding.taskTextContent.text = "${task.order}. ${task.content}"
        binding.taskTextContent.apply {
            text = task.content
            isVisible = task.content.isNotEmpty()
        }
    }

    private fun bindSelection(task: NotePresentationModel) {
        if (task.isSelected) {
            setBackgroundSelected()
        } else {
            setBackgroundTransparent()
        }
    }

    private fun setBackgroundTransparent() {
        binding.taskCardview.isChecked = false
        binding.taskCardview.strokeWidth =
            1.5f.fromDpToPixel(binding.taskCardview.context).toInt()
    }

    private fun setBackgroundSelected() {
        binding.taskCardview.isChecked = true
        binding.taskCardview.strokeWidth =
            2.5f.fromDpToPixel(binding.taskCardview.context).toInt()
    }

    private fun bindTitle(task: NotePresentationModel) {
        with(binding.taskTextTitle) {
            isVisible = !task.title.isNullOrEmpty()
            text = task.title
        }
    }


}