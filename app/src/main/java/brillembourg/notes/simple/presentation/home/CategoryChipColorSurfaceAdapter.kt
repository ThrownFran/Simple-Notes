package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import brillembourg.notes.simple.databinding.ItemCategoryChipColorSurfaceBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class CategoryChipColorSurfaceAdapter(
    private val onClick: ((position: CategoryPresentationModel) -> Unit)? = null,
    private val onLongClick: ((position: CategoryPresentationModel) -> Unit)? = null,
) : ListAdapter<CategoryPresentationModel, CategoryChipViewColorSurfaceHolder>(
    setupCategoryDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryChipViewColorSurfaceHolder {
        return CategoryChipViewColorSurfaceHolder(
            onClick = { position: Int -> onClick?.invoke(currentList[position]) },
            onLongClick = { position: Int -> onLongClick?.invoke(currentList[position]) },
            binding = ItemCategoryChipColorSurfaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryChipViewColorSurfaceHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

