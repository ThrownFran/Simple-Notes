package brillembourg.notes.simple.presentation.categories

import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import brillembourg.notes.simple.presentation.home.delete.NoteDeletionState
import brillembourg.notes.simple.presentation.trash.SelectionModeActive
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoriesUiState(
    val categoryList: CategoryList = CategoryList(),
    val selectionMode: SelectionModeActive = SelectionModeActive(),
    val deleteConfirmation: NoteDeletionState.ConfirmDeleteDialog? = null,
    val createCategory: CreateCategory = CreateCategory(),
) : Parcelable

@Parcelize
data class CategoryList(
    val data: List<CategoryPresentationModel> = emptyList(),
    val mustRender: Boolean = false //To avoid rendering set false
) : Parcelable

@Parcelize
data class CreateCategory(var isEnabled: Boolean = false, var name: String = "") : Parcelable,
    BaseObservable() {


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
//@Parcelize
//data class SelectionMode(
//    val size: Int
//) : Parcelable

/*Show confirm to archive notes*/
@Parcelize
data class ShowDeleteCategoriesConfirmationState(
    val categoriesToDeleteSize: Int
) : Parcelable

