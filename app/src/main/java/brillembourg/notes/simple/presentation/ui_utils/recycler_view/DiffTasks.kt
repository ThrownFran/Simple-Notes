package brillembourg.notes.simple.presentation.ui_utils

import androidx.recyclerview.widget.DiffUtil
import brillembourg.notes.simple.presentation.models.NotePresentationModel

fun setupTaskDiffCallback() = object : DiffUtil.ItemCallback<NotePresentationModel>() {
    override fun areItemsTheSame(
        oldItem: NotePresentationModel,
        newItem: NotePresentationModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: NotePresentationModel,
        newItem: NotePresentationModel
    ): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(
        oldItem: NotePresentationModel,
        newItem: NotePresentationModel
    ): Any? {
        return super.getChangePayload(oldItem, newItem)
    }
}