package brillembourg.notes.simple.ui.trash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.home.NoteViewHolder
import brillembourg.notes.simple.ui.home.setupTaskDiffCallback
import brillembourg.notes.simple.ui.models.TaskPresentationModel

class ArchivedTaskAdapter(
    val recyclerView: RecyclerView,
    val onClick: (TaskPresentationModel, View) -> Unit,
    val onSelection: () -> Unit,
) : ListAdapter<TaskPresentationModel, NoteViewHolder>(setupTaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            getCurrentList = { currentList },
            onClick = onClick,
            onSelected = onSelection
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

