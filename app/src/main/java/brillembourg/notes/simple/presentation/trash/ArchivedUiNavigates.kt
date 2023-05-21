package brillembourg.notes.simple.presentation.trash

import android.os.Parcelable
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import kotlinx.parcelize.Parcelize

sealed class ArchivedUiNavigates {

    @Parcelize
    data class NavigateToEditNote(
        val mustConsume: Boolean = false,
        val taskIndex: Int? = null,
        val notePresentationModel: NotePresentationModel? = null,
    ) : ArchivedUiNavigates(), Parcelable

    object Idle : ArchivedUiNavigates()
}