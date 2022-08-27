package brillembourg.notes.simple.presentation.models

import android.os.Parcelable
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteWithCategoriesPresentationModel(
    val note: NotePresentationModel,
    val categoryList: List<CategoryPresentationModel>
) : Parcelable