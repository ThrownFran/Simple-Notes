package brillembourg.notes.simple.presentation.trash

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class TrashDialogsState {

    /*Show confirm to archive notes*/
    @Parcelize
    data class ShowDeleteNotesConfirmation(
        val tasksToDeleteSize: Int
    ) : Parcelable

    object Idle : TrashDialogsState()
}