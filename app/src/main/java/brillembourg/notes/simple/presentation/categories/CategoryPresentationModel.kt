package brillembourg.notes.simple.presentation.categories

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.Category
import brillembourg.notes.simple.presentation.models.HasOrder
import brillembourg.notes.simple.presentation.models.IsSelectable
import kotlinx.parcelize.Parcelize

@Parcelize
class CategoryPresentationModel(
    val id: Long = -1L,
    var name: String,
    override var order: Int,
    override var isSelected: Boolean
) : Parcelable, HasOrder, IsSelectable


fun Category.toPresentation(): CategoryPresentationModel {
    return CategoryPresentationModel(id, name, order, false)
}

fun CategoryPresentationModel.toDomain(): Category {
    return Category(id, name, order)
}