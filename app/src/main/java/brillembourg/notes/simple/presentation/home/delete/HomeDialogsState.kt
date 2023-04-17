package brillembourg.notes.simple.presentation.home.delete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class HomeDialogsState {

    /*Show confirm to archive notes*/
    @Parcelize
    data class DeleteCategoriesConfirmation(
        val tasksToDeleteSize: Int
    ) : HomeDialogsState(), Parcelable

    /*Show confirm to archive notes*/
    @Parcelize
    data class ShowArchiveNotesConfirmationState(
        val tasksToArchiveSize: Int
    ) : HomeDialogsState(), Parcelable

    object Idle : HomeDialogsState()
}