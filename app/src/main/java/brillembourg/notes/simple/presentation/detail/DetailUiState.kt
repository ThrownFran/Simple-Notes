package brillembourg.notes.simple.presentation.detail

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

data class DetailUiState(
    val isNewTask: Boolean = true, //If is new note or an editing note
    val isArchivedTask: Boolean = false,
    var userInput: UserInput = UserInput(),
    val navigateBack: Boolean = false,
    val focusInput: Boolean = false,
    val unFocusInput: Boolean = false,
) {
    fun getOnInputChangedFlow(): Flow<UserInput> {
        return callbackFlow {
            userInput.onInputChanged = { trySend(it) }
            awaitClose { userInput.onInputChanged = null }
        }.conflate()
    }
}

/**
 * Two way data binding with title and content
 */
data class UserInput(
    var title: String = "",
    var content: String = "",
    var onInputChanged: ((UserInput) -> Unit)? = null
) : BaseObservable() {

//    fun isNotEmpty() =
//        (title != null && title.isNotEmpty()) || (content != null && content.isNotEmpty())

    fun isNullOrEmpty() = title.isNullOrEmpty() && content.isNullOrEmpty()

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