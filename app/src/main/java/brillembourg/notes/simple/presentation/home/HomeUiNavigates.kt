package brillembourg.notes.simple.presentation.home

import android.os.Parcelable
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import kotlinx.parcelize.Parcelize

sealed class HomeUiNavigates {

    @Parcelize
    data class NavigateToAddNote(val content: String? = null) : HomeUiNavigates(), Parcelable

    @Parcelize
    data class NavigateToEditNote(
        val mustConsume: Boolean = false,
        val taskIndex: Int? = null,
        val notePresentationModel: NotePresentationModel? = null,
    ) : HomeUiNavigates(), Parcelable

    object Idle : HomeUiNavigates()
}