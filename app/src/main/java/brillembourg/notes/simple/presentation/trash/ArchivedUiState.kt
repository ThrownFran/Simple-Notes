package brillembourg.notes.simple.presentation.trash

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.NoteLayout
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArchivedUiState(
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive? = null,
    val noteActions: NoteActions = NoteActions(),
    val emptyNote: ArchivedViewModel.EmptyNote = ArchivedViewModel.EmptyNote.None
) : Parcelable {

    @Parcelize
    data class NoteActions(
        val copyToClipboard: String? = null,
        val shareNoteAsString: String? = null,
    ) : Parcelable

}

