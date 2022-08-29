package brillembourg.notes.simple.presentation.categories

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.presentation.models.HasOrder
import brillembourg.notes.simple.presentation.models.IsSelectable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryPresentationModel(
    val id: Long = -1L,
    var name: String,
    override var order: Int,
    override var isSelected: Boolean = false,
    var isEditing: Boolean = false
) : Parcelable, HasOrder, IsSelectable


fun Category.toPresentation(): CategoryPresentationModel {
    return CategoryPresentationModel(id, name, order)
}

fun CategoryPresentationModel.toDomain(): Category {
    return Category(id, name, order)
}

fun List<CategoryPresentationModel>.toDiplayOrder(): List<CategoryPresentationModel> {
    return this.sortedBy { it.order }.asReversed()
}