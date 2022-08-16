package brillembourg.notes.simple.ui.utils

import androidx.recyclerview.widget.DiffUtil
import brillembourg.notes.simple.ui.models.TaskPresentationModel

fun setupTaskDiffCallback() = object : DiffUtil.ItemCallback<TaskPresentationModel>() {
    override fun areItemsTheSame(
        oldItem: TaskPresentationModel,
        newItem: TaskPresentationModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: TaskPresentationModel,
        newItem: TaskPresentationModel
    ): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(
        oldItem: TaskPresentationModel,
        newItem: TaskPresentationModel
    ): Any? {
        return super.getChangePayload(oldItem, newItem)
    }
}