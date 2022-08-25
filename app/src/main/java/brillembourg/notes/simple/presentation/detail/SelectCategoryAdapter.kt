package brillembourg.notes.simple.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import brillembourg.notes.simple.databinding.ItemSelectCategoryBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class SelectCategoryAdapter(
) : ListAdapter<CategoryPresentationModel, SelectCategoryViewHolder>(setupCategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCategoryViewHolder {
        return SelectCategoryViewHolder(
            binding = ItemSelectCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SelectCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

