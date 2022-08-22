package brillembourg.notes.simple.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.Draggable
import brillembourg.notes.simple.presentation.ui_utils.DraggableImp
import brillembourg.notes.simple.presentation.ui_utils.setupTaskDiffCallback

class NoteAdapter(
    dragAndDropDirs: Int,
    val recyclerView: RecyclerView,
    val onClick: (task: NotePresentationModel, clickedView: View) -> Unit,
    val onSelection: () -> Unit,
    val onReorderSuccess: (reorderedTaskList: List<NotePresentationModel>) -> Unit,
    val onReorderCanceled: () -> Unit
) : ListAdapter<NotePresentationModel, NoteViewHolder>(setupTaskDiffCallback()),
    Draggable by DraggableImp(dragAndDropDirs) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onReadyToDrag = onStartDrag(),
            onClick = onClick,
            onSelected = onSelection,
            getCurrentList = { currentList }
        )
    }

    private fun onStartDrag() = { noteViewHolder: NoteViewHolder ->
        startDrag(
            recyclerView = recyclerView,
            viewHolder = noteViewHolder,
            onGetCurrentList = { currentList },
            onSubmitList = { noteList -> submitList(noteList) },
            onReorderSuccess = onReorderSuccess,
            onReorderCanceled = onReorderCanceled
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

