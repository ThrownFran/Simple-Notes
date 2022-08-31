package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import brillembourg.notes.simple.databinding.ItemHeaderFilteredCategoriesBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class HeaderAdapter(
    val filteredCategories: MutableList<CategoryPresentationModel>,
    private val onClick: (() -> Unit)? = null,
) : RecyclerView.Adapter<HeaderFilterCategoriesHolder>() {


    override fun getItemCount(): Int {
        return 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeaderFilterCategoriesHolder {
        return HeaderFilterCategoriesHolder(
            onClick = onClick,
            binding = ItemHeaderFilteredCategoriesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HeaderFilterCategoriesHolder, position: Int) {
        holder.bind(filteredCategories)
    }

}

class HeaderFilterCategoriesHolder(
    private val binding: ItemHeaderFilteredCategoriesBinding,
    private val onClick: (() -> Unit)? = null,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        (itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true

        binding.root.setOnClickListener {
            onClick?.invoke()
        }
    }

    fun bind(filteredCategories: List<CategoryPresentationModel>) {
        (itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true

        if (binding.mainRecyclerCategoriesFilter.adapter == null) {
            binding.mainRecyclerCategoriesFilter.apply {
                layoutManager = FlexboxLayoutManager(context)
                    .apply {
                        flexDirection = FlexDirection.ROW
                        justifyContent = JustifyContent.FLEX_START
                        flexWrap = FlexWrap.WRAP
                    }

                adapter = CategoryChipColorSurfaceAdapter(onClick = {
                    onClick?.invoke()
                }).apply { submitList(filteredCategories.toDiplayOrder()) }
            }
        } else {
            (binding.mainRecyclerCategoriesFilter.adapter as CategoryChipColorSurfaceAdapter).apply {
                submitList(filteredCategories.toDiplayOrder())
//                notifyItemRangeChanged(0,filteredCategories.size,filteredCategories)
            }
        }
    }
}