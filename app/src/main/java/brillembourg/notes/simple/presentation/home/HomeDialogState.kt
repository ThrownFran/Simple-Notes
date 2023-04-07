package brillembourg.notes.simple.presentation.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class HomeDialogState {

    /*Show confirm to archive notes*/
    @Parcelize
    data class DeleteCategoriesConfirmation(
        val tasksToDeleteSize: Int
    ) : HomeDialogState(), Parcelable

    /*Show confirm to archive notes*/
    @Parcelize
    data class ShowArchiveNotesConfirmationState(
        val tasksToArchiveSize: Int
    ) : HomeDialogState(), Parcelable

    object Idle : HomeDialogState()
}