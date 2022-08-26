package brillembourg.notes.simple.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import brillembourg.notes.simple.databinding.ItemSelectCategoryBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class SelectCategoryAdapter(
    private val onCheckChanged: (category: CategoryPresentationModel, isChecked: Boolean) -> Unit
) : ListAdapter<CategoryPresentationModel, SelectCategoryViewHolder>(setupCategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCategoryViewHolder {
        return SelectCategoryViewHolder(
            binding = ItemSelectCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onCheckChanged = { categoryPosition, isChecked ->
                onCheckChanged.invoke(
                    currentList[categoryPosition],
                    isChecked
                )
            }
        )
    }

    override fun onBindViewHolder(holder: SelectCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

