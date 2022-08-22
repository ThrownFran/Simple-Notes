package brillembourg.notes.simple.presentation.models

import android.os.Parcelable
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Note
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotePresentationModel(
    val id: Long = -1L,
    var title: String? = null,
    var content: String,
    val dateInLocal: String,
    var order: Int,
    var isArchived: Boolean = false,
    var isSelected: Boolean = false
) : Parcelable

fun NotePresentationModel.toDomain(dateProvider: DateProvider): Note {
    return Note(
        id,
        title = title,
        content = content,
        order = order,
        date = dateProvider.formatLocalDateToTime(dateInLocal),
        isArchived = isArchived
    )
}

fun Note.toPresentation(dateProvider: DateProvider): NotePresentationModel {
    return NotePresentationModel(
        id = id,
        title = title,
        content = content,
        order = order,
        dateInLocal = dateProvider.formatTimeToLocalDate(date),
        isArchived = isArchived
    )
}