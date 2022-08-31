package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemCategoryChipColorSecondaryBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class CategoryChipColorSecondaryAdapter(
    private val onClick: ((position: CategoryPresentationModel) -> Unit)? = null,
    private val onLongClick: ((position: CategoryPresentationModel) -> Unit)? = null,
) : ListAdapter<CategoryPresentationModel, CategoryChipColorSecondaryVariantHolder>(
    setupCategoryDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryChipColorSecondaryVariantHolder {
        return CategoryChipColorSecondaryVariantHolder(
            onClick = { position: Int -> onClick?.invoke(currentList[position]) },
            onLongClick = { position: Int -> onLongClick?.invoke(currentList[position]) },
            binding = ItemCategoryChipColorSecondaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: CategoryChipColorSecondaryVariantHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

}

class CategoryChipColorSecondaryVariantHolder(
    private val binding: ItemCategoryChipColorSecondaryBinding,
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