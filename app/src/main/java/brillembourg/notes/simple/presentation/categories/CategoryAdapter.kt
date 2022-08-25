package brillembourg.notes.simple.presentation.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemCategoryBinding
import brillembourg.notes.simple.presentation.ui_utils.Draggable
import brillembourg.notes.simple.presentation.ui_utils.ItemTouchDraggableImp
import brillembourg.notes.simple.presentation.ui_utils.setupCategoryDiffCallback

class CategoryAdapter(
    private val isEditing: () -> Boolean,
    private val recyclerView: RecyclerView,
    private val onClick: (task: CategoryPresentationModel) -> Unit,
    private val onSelection: () -> Unit,
    private val onReorderSuccess: (reorderedTaskList: List<CategoryPresentationModel>) -> Unit,
    private val onReorderCanceled: () -> Unit
) : ListAdapter<CategoryPresentationModel, CategoryViewHolder>(setupCategoryDiffCallback()),
    Draggable<CategoryPresentationModel> by ItemTouchDraggableImp(ItemTouchHelper.UP or ItemTouchHelper.DOWN) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            isEditing = isEditing,
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

