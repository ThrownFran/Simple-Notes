package brillembourg.notes.simple.presentation.categories

import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemCategoryBinding
import brillembourg.notes.simple.presentation.custom_views.fromDpToPixel
import brillembourg.notes.simple.presentation.ui_utils.Selectable
import brillembourg.notes.simple.presentation.ui_utils.SelectableImp

class CategoryViewHolder(
    private val getCurrentList: () -> List<CategoryPresentationModel>,
    private val binding: ItemCategoryBinding,
    onClick: ((CategoryPresentationModel) -> Unit)? = null,
    private val onSelected: (() -> Unit)? = null,
    private val onReadyToDrag: ((CategoryViewHolder) -> Unit)? = null
) : RecyclerView.ViewHolder(binding.root),
    Selectable<CategoryPresentationModel> by SelectableImp(getCurrentList) {

    init {
        setupClickListeners()
        setupSelection(
            bindSelection = { categoryPresentationModel -> bindSelection(categoryPresentationModel) },
            onSelected = { onSelected?.invoke() },
            onClickWithNoSelection = onClick,
            onReadyToDrag = { onReadyToDrag?.invoke(this) }
        )
    }

    private fun setupClickListeners() {

        binding.categoryCardview.setOnClickListener {
            onItemClick(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
        }

        binding.categoryCardview.setOnLongClickListener {
            onItemSelection(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
            true
        }
    }

    fun bind(category: CategoryPresentationModel) {
        bindName(category)
        bindSelection(category)
    }

    private fun bindName(category: CategoryPresentationModel) {
        binding.categoryTextName.text = category.name
    }

    private fun bindSelection(task: CategoryPresentationModel) {
        if (task.isSelected) {
            setBackgroundSelected()
        } else {
            setBackgroundTransparent()
        }
    }

    private fun setBackgroundTransparent() {
        binding.categoryCardview.isChecked = false
        binding.categoryCardview.strokeWidth =
            1f.fromDpToPixel(binding.categoryCardview.context).toInt()
    }

    private fun setBackgroundSelected() {
        binding.categoryCardview.isChecked = true
        binding.categoryCardview.strokeWidth =
            3f.fromDpToPixel(binding.categoryCardview.context).toInt()
    }


}