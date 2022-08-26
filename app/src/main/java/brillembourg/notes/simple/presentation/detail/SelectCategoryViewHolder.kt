package brillembourg.notes.simple.presentation.detail

import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemSelectCategoryBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel

class SelectCategoryViewHolder(
    private val binding: ItemSelectCategoryBinding,
    private val onCheckChanged: (categoryPosition: Int, isChecked: Boolean) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.selectCategoryCheck.setOnCheckedChangeListener { compoundButton, b ->
            onCheckChanged.invoke(bindingAdapterPosition, b)
        }
    }

    fun bind(category: CategoryPresentationModel) {
        bindName(category)
        bindChecked(category)
    }

    private fun bindChecked(category: CategoryPresentationModel) {
        binding.selectCategoryCheck.setChecked(category.isSelected, false)
    }

    private fun bindName(category: CategoryPresentationModel) {
        binding.selectCategoryTextName.text = category.name
    }


}