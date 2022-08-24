package brillembourg.notes.simple.presentation.categories

import android.os.Parcelable
import brillembourg.notes.simple.presentation.home.ShowDeleteNotesConfirmationState
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoriesUiState(
    val list: List<CategoryPresentationModel> = emptyList(),
    val selectionModeActive: SelectionModeActive? = null,
    val showDeleteNotesConfirmationState: ShowDeleteNotesConfirmationState? = null,
    val createCategory: Boolean = false
) : Parcelable


/*Notes are selected and contextual bar is shown*/
@Parcelize
data class SelectionModeActive(
    val size: Int
) : Parcelable

/*Show confirm to archive notes*/
@Parcelize
data class ShowDeleteCategoriesConfirmationState(
    val categoriesToDeleteSize: Int
) : Parcelable