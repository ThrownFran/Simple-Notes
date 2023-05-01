package brillembourg.notes.simple.presentation.home.delete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class NoteDeletionState {

    /*Show confirm to archive notes*/
    @Parcelize
    data class ConfirmArchiveDialog(
        val tasksToDeleteSize: Int
    ) : NoteDeletionState(), Parcelable

    @Parcelize
    data class ConfirmUnArchiveDialog(
        val tasksToDeleteSize: Int
    ) : NoteDeletionState(), Parcelable

    /*Show confirm to archive notes*/
    @Parcelize
    data class ConfirmDeleteDialog(
        val tasksToArchiveSize: Int
    ) : NoteDeletionState(), Parcelable


    object Idle : NoteDeletionState()
}