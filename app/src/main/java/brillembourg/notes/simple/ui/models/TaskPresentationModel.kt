package brillembourg.notes.simple.ui.models

import android.os.Parcelable
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskPresentationModel (val id: Long, var content: String, val dateInLocal: String): Parcelable

fun TaskPresentationModel.toDomain (dateProvider: DateProvider): Task {
    return Task(id,content,dateProvider.formatLocalDateToTime(dateInLocal))
}