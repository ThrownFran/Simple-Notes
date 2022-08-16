package brillembourg.notes.simple.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel

class TaskAdapter(
    dragAndDropDirs: Int,
    val recyclerView: RecyclerView,
    val onClick: (task: TaskPresentationModel, clickedView: View) -> Unit,
    val onSelection: () -> Unit,
    val onReorderSuccess: (reorderedTaskList: List<TaskPresentationModel>) -> Unit,
    val onReorderCanceled: () -> Unit
) : ListAdapter<TaskPresentationModel, NoteViewHolder>(setupTaskDiffCallback()) {

    var dragAndDrogList: List<TaskPresentationModel>? = null
    var isDragging = false
    var itemTouchHelper = setupDragAndDropTouchHelper(dragAndDropDirs)

//    override fun submitList(list: List<TaskPresentationModel>?) {
//        super.submitList(list?.let { ArrayList(it) })
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onReadyToDrag = { startDrag(it) },
            onClick = onClick,
            onSelected = onSelection,
            getCurrentList = { currentList }
        )
    }

    private fun startDrag(viewHolder: NoteViewHolder) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

