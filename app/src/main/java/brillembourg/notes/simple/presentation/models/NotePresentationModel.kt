package brillembourg.notes.simple.presentation.models

import android.os.Parcelable
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.models.Note
import brillembourg.notes.simple.domain.models.NoteWithCategories
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toPresentation
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotePresentationModel(
    override val id: Long = -1L,
    var title: String? = null,
    var content: String,
    val dateInLocal: String,
    override var order: Int,
    var isArchived: Boolean = false,
    override var isSelected: Boolean = false,
    val categories: List<CategoryPresentationModel>
) : Parcelable, HasOrder, IsSelectable

fun NotePresentationModel.toDomain(dateProvider: DateProvider): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        order = order,
        date = dateProvider.formatLocalDateToTime(dateInLocal),
        isArchived = isArchived
    )
}

fun NoteWithCategories.toPresentation(dateProvider: DateProvider): NotePresentationModel {
    return NotePresentationModel(
        id = note.id,
        title = note.title,
        content = note.content,
        dateInLocal = dateProvider.formatTimeToLocalDate(note.date),
        order = note.order,
        isArchived = note.isArchived,
        isSelected = false,
        categories = categories.map { it.toPresentation() })
}

interface HasOrder {
    var order: Int
}

interface IsSelectable {
    val id: Long
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