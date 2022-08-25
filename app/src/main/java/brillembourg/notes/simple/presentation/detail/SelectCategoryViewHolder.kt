package brillembourg.notes.simple.presentation.detail

import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemSelectCategoryBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel

class SelectCategoryViewHolder(
    private val binding: ItemSelectCategoryBinding,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
    }

    fun bind(category: CategoryPresentationModel) {
        bindName(category)
    }

    private fun bindName(category: CategoryPresentationModel) {
        binding.selectCategoryTextName.text = category.name
    }


}