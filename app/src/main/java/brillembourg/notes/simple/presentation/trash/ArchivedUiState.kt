package brillembourg.notes.simple.presentation.trash

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArchivedUiState(
    val noteList: List<NotePresentationModel> = ArrayList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive? = null,
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowDeleteNotesConfirmation? = null,
    val copyToClipboard: String? = null,
    val shareNoteAsString: String? = null
) : Parcelable {

    /*Show confirm to archive notes*/
    @Parcelize
    data class ShowDeleteNotesConfirmation(
        val tasksToDeleteSize: Int
    ) : Parcelable

    @Parcelize
    data class NavigateToEditNote(
        val mustConsume: Boolean = false,
        val taskIndex: Int? = null,
        val notePresentationModel: NotePresentationModel? = null,
    ) : Parcelable

}

