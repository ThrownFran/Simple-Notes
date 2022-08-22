package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupDragAndDropTouchHelper
import brillembourg.notes.simple.presentation.ui_utils.setupTaskDiffCallback

class NoteAdapter(
    dragAndDropDirs: Int,
    val recyclerView: RecyclerView,
    val onClick: (task: NotePresentationModel, clickedView: View) -> Unit,
    val onSelection: () -> Unit,
    val onStartDrag: (() -> Unit)? = null,
    val onReorderSuccess: (reorderedTaskList: List<NotePresentationModel>) -> Unit,
    val onReorderCanceled: () -> Unit
) : ListAdapter<NotePresentationModel, NoteViewHolder>(setupTaskDiffCallback()) {

    var dragAndDrogList: List<NotePresentationModel>? = null
    var isDragging = false
    var itemTouchHelper = setupDragAndDropTouchHelper(dragAndDropDirs)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onReadyToDrag = {
                onStartDrag?.invoke()
                startDrag(it)
            },
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

