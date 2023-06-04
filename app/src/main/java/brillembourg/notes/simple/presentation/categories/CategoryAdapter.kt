package brillembourg.notes.simple.presentation.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemCategoryBinding
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.Draggable
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.ItemTouchDraggableImp
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class CategoryAdapter(
    private val recyclerView: RecyclerView,
    private val onRename: (name: String, CategoryPresentationModel) -> Unit,
    private val onClick: (task: CategoryPresentationModel) -> Unit,
    private val onSelection: (isSelected: Boolean, id: Long) -> Unit,
    private val onReorderSuccess: (reorderedTaskList: List<CategoryPresentationModel>) -> Unit,
    private val onReorderCanceled: () -> Unit
) : ListAdapter<CategoryPresentationModel, CategoryViewHolder>(setupCategoryDiffCallback()),
    Draggable<CategoryPresentationModel> by ItemTouchDraggableImp(recyclerView) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            onRename = onRename,
            binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onReadyToDrag = onStartDrag(),
            onClick = onClick,
            onSelected = onSelection,
            getCurrentList = { currentList }
        )
    }

    private fun onStartDrag() = { noteViewHolder: CategoryViewHolder ->
        startDrag(
            recyclerView = recyclerView,
            viewHolder = noteViewHolder,
            onGetCurrentList = { currentList },

            onSubmitList = { noteList, submitSuccess ->
                submitList(noteList) {
                    submitSuccess() //Commit callback
                }
            },

            onReorderSuccess = onReorderSuccess,
            onReorderCanceled = onReorderCanceled
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

