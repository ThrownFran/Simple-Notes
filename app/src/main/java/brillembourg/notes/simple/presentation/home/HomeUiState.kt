package brillembourg.notes.simple.presentation.home

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUiState(
    val noteList: NoteList = NoteList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive? = null,
    val navigateToAddNote: NavigateToAddNote? = null,
    var navigateToEditNote: NavigateToEditNote = NavigateToEditNote(false),
    val showArchiveNotesConfirmation: ShowArchiveNotesConfirmationState? = null,
    val showDeleteNotesConfirmation: DeleteCategoriesConfirmation? = null,
    val copyToClipboard: String? = null,
    val shareNoteAsString: String? = null
) : Parcelable

@Parcelize
data class NavigateToAddNote(val content: String? = null) : Parcelable

@Parcelize
data class NoteList(
    val notes: List<NotePresentationModel> = ArrayList(),
    val mustRender: Boolean = false //To avoid rendering set false
) : Parcelable

/*Show confirm to archive notes*/
@Parcelize
data class ShowArchiveNotesConfirmationState(
    val tasksToArchiveSize: Int
) : Parcelable

/*Show confirm to archive notes*/
@Parcelize
data class DeleteCategoriesConfirmation(
    val tasksToDeleteSize: Int
) : Parcelable

@Parcelize
data class NavigateToEditNote(
    val mustConsume: Boolean = false,
    val taskIndex: Int? = null,
    val notePresentationModel: NotePresentationModel? = null,
) : Parcelable

/*Notes are selected and contextual bar is shown*/
@Parcelize
data class SelectionModeActive(
    val size: Int
) : Parcelable