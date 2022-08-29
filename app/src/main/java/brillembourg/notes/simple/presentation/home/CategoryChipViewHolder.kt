package brillembourg.notes.simple.presentation.home

import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemCategoryChipBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel

class CategoryChipViewHolder(
    private val binding: ItemCategoryChipBinding,
    private val onClick: ((position: Int) -> Unit)? = null,
    private val onLongClick: ((position: Int) -> Unit)? = null
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.categoryChip.apply {
            setOnClickListener { onClick?.invoke(bindingAdapterPosition) }

            setOnLongClickListener {
                onLongClick?.invoke(bindingAdapterPosition)
                true
            }
        }
    }

    fun bind(category: CategoryPresentationModel) {
        bindName(category)
    }

    private fun bindName(category: CategoryPresentationModel) {
        binding.categoryChip.text = category.name
    }


}