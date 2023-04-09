package brillembourg.notes.simple.presentation.home.delete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class DeleteNoteState {

    /*Show confirm to archive notes*/
    @Parcelize
    data class DeleteCategoriesConfirmation(
        val tasksToDeleteSize: Int
    ) : DeleteNoteState(), Parcelable

    /*Show confirm to archive notes*/
    @Parcelize
    data class ShowArchiveNotesConfirmationState(
        val tasksToArchiveSize: Int
    ) : DeleteNoteState(), Parcelable

    object Idle : DeleteNoteState()
}