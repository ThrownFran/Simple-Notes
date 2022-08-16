package brillembourg.notes.simple.ui.models

import android.os.Parcelable
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskPresentationModel(
    val id: Long = -1L,
    var title: String? = null,
    var content: String,
    val dateInLocal: String,
    var order: Int,
    var isArchived: Boolean = false,
    var isSelected: Boolean = false
): Parcelable

fun TaskPresentationModel.toDomain (dateProvider: DateProvider): Task {
    return Task(
        id,
        title = title,
        content = content,
        order = order,
        date = dateProvider.formatLocalDateToTime(dateInLocal),
        isArchived = isArchived
    )
}

fun Task.toPresentation (dateProvider: DateProvider): TaskPresentationModel {
    return TaskPresentationModel(
        id = id,
        title = title,
        content = content,
        order = order,
        dateInLocal = dateProvider.formatTimeToLocalDate(date),
        isArchived = isArchived
    )
}