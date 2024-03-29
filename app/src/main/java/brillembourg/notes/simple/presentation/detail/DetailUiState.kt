package brillembourg.notes.simple.presentation.detail

import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailUiState(
    val isNewTask: Boolean = true, //If is new note or an editing note
    val isArchivedTask: Boolean = false,
    var userInput: UserInput = UserInput(),
    val navigateBack: Boolean = false,
    val focusInput: Boolean = false,
    val unFocusInput: Boolean = false,
    val noteCategories: List<CategoryPresentationModel> = emptyList(),
    var selectCategories: SelectCategories = SelectCategories(),
    val lastEdit: String = ""
) : Parcelable {

    fun getOnInputChangedFlow(): Flow<UserInput> {
        return userInput.getOnInputChangedFlow()
    }
}

@Parcelize
data class SelectCategories(
    val isCategoryMenuAvailable: Boolean = false,
    val navigate: Boolean = false,
    val isShowing: Boolean = false,
    val categories: List<CategoryPresentationModel> = emptyList()
) : Parcelable

/**
 * Two way data binding with title and content
 */
@Parcelize
data class UserInput(
    var title: String = "",
    var content: String = "",
) : BaseObservable(), Parcelable {

    @IgnoredOnParcel
    private var onInputChanged: ((UserInput) -> Unit)? = null

    fun isNullOrEmpty() = title.isNullOrEmpty() && content.isNullOrEmpty()

    fun getOnInputChangedFlow(): Flow<UserInput> {
        return callbackFlow {
            onInputChanged = { trySend(it) }
            awaitClose { onInputChanged = null }
        }.conflate()
    }

    @Bindable
    fun getTitleBinding(): String {
        return title
    }

    fun setTitleBinding(value: String) {
        // Avoids infinite loops.
        if (title != value) {
            title = value
            // React to the change.
            onInputChanged?.invoke(this)
            // Notify observers of a new value.
            notifyPropertyChanged(BR.titleBinding)
        }
    }

    @Bindable
    fun getContentBinding(): String {
        return content
    }

    fun setContentBinding(value: String) {
        // Avoids infinite loops.
        if (content != value) {
            content = value
            // React to the change.
            onInputChanged?.invoke(this)
            // Notify observers of a new value.
            notifyPropertyChanged(BR.contentBinding)
        }
    }
}


