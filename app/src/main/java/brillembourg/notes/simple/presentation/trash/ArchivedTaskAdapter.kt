package brillembourg.notes.simple.presentation.trash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemNoteBinding
import brillembourg.notes.simple.presentation.home.NoteViewHolder
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.setupTaskDiffCallback

class ArchivedTaskAdapter(
    val recyclerView: RecyclerView,
    val onClick: (NotePresentationModel) -> Unit,
    val onSelection: () -> Unit,
) : ListAdapter<NotePresentationModel, NoteViewHolder>(setupTaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            getCurrentList = { currentList },
            onClick = onClick,
            onSelected = onSelection
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

