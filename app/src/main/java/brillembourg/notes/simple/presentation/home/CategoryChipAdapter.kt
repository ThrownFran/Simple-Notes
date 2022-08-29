package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import brillembourg.notes.simple.databinding.ItemCategoryChipBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class CategoryChipAdapter(
    val onClick: ((position: CategoryPresentationModel) -> Unit)? = null,
) : ListAdapter<CategoryPresentationModel, CategoryChipViewHolder>(setupCategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryChipViewHolder {
        return CategoryChipViewHolder(
            onClick = { position: Int -> onClick?.invoke(currentList[position]) },
            binding = ItemCategoryChipBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

