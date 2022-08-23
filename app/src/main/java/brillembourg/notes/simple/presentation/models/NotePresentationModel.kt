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
    override var order: Int,
    var isArchived: Boolean = false,
    override var isSelected: Boolean = false
) : Parcelable, HasOrder, IsSelectable

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

interface HasOrder {
    var order: Int
}

interface IsSelectable {
    var isSelected: Boolean
}

fun NotePresentationModel.toCopyString(): String {
    return StringBuilder(title ?: "")
        .append((if (title?.isNotEmpty() == true) "\n\n" else ""))
        .append(content)
        .toString()
}

fun List<NotePresentationModel>.toCopyString(): String {
    var generatedText = ""
    this.map {
        generatedText = generatedText + it.toCopyString() + "\n\n"
    }
    return generatedText
}