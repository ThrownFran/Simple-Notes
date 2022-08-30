package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemHeaderFilteredCategoriesBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel

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

