package brillembourg.notes.simple.presentation.categories

import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import brillembourg.notes.simple.presentation.home.ShowDeleteNotesConfirmationState
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoriesUiState(
    val list: List<CategoryPresentationModel> = emptyList(),
    val selectionModeActive: SelectionModeActive? = null,
    val showDeleteNotesConfirmationState: ShowDeleteNotesConfirmationState? = null,
    val createCategory: CreateCategory = CreateCategory()
) : Parcelable

@Parcelize
data class CreateCategory(var isEnabled: Boolean = false, var name: String = "") : Parcelable,
    BaseObservable() {

    fun clear() {
        isEnabled = false
        name = ""
    }

    @Bindable
    fun getCreateCategoryNameBinding(): String {
        return name
    }

    fun setCreateCategoryNameBinding(value: String) {
        // Avoids infinite loops.
        if (name != value) {
            name = value
            // React to the change.
            // Notify observers of a new value.
            notifyPropertyChanged(BR.createCategoryNameBinding)
        }
    }

}

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