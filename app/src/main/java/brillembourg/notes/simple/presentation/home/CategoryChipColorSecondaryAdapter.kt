package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import brillembourg.notes.simple.databinding.ItemCategoryChipColorSecondaryBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class CategoryChipColorSecondaryAdapter(
    private val onClick: ((position: CategoryPresentationModel) -> Unit)? = null,
    private val onLongClick: ((position: CategoryPresentationModel) -> Unit)? = null,
) : ListAdapter<CategoryPresentationModel, CategoryChipViewColorSecondaryVariantHolder>(
    setupCategoryDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryChipViewColorSecondaryVariantHolder {
        return CategoryChipViewColorSecondaryVariantHolder(
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
        holder: CategoryChipViewColorSecondaryVariantHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

}

