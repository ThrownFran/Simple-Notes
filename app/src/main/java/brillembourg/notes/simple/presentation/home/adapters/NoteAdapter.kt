package brillembourg.notes.simple.presentation.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.home.NoteViewHolder
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.Draggable
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.ItemTouchDraggableImp
import brillembourg.notes.simple.presentation.ui_utils.setupTaskDiffCallback

class NoteAdapter(
    dragAndDropDirs: Int,
    private val recyclerView: RecyclerView,
    private val isDragEnabled: Boolean = false,
    private val onClick: (task: NotePresentationModel) -> Unit,
    private val onSelection: (isSelected: Boolean, id: Long) -> Unit,
    private val onReorderSuccess: (reorderedTaskList: List<NotePresentationModel>) -> Unit,
    private val onReorderCanceled: () -> Unit
) : ListAdapter<NotePresentationModel, NoteViewHolder>(setupTaskDiffCallback()),
    Draggable<NotePresentationModel> by ItemTouchDraggableImp(recyclerView, dragAndDropDirs) {

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
        if (isDragEnabled) {
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
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

