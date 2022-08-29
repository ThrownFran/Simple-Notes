package brillembourg.notes.simple.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

//TODO relocate
fun setupSelectCategoriesAdapter(
    recycler: RecyclerView,
    allCategories: List<CategoryPresentationModel>,
    noteCategories: List<Long>,
    onCheckChanged: (category: CategoryPresentationModel, isChecked: Boolean) -> Unit
) {

    //copy to avoid modifying detailuistate
    val selectCategories = allCategories.toMutableList().map { it.copy() }

    //check categories from note
    selectCategories.forEach { category ->
        if (noteCategories.contains(category.id)) {
            category.isSelected = true
        }
    }

    if (recycler.adapter == null) {
        recycler.apply {
            adapter = SelectCategoryAdapter(onCheckChanged)
                .apply {
                    submitList(selectCategories)
                }
            layoutManager = LinearLayoutManager(context)
        }
    } else {
        (recycler.adapter as SelectCategoryAdapter)
            .submitList(selectCategories)
    }
}

