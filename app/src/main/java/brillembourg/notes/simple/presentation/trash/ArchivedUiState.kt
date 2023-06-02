package brillembourg.notes.simple.presentation.trash

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.home.NoteList
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArchivedUiState(
    val noteList: NoteList = NoteList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive = SelectionModeActive(),
    val noteActions: NoteActions = NoteActions(),
    val isLoading: Boolean = noteList.hasLoaded.not()
) : Parcelable {

    val emptyNote: EmptyNote
        get() = when {
            isLoading.not() && noteList.key.isEmpty() && noteList.notes.isEmpty() -> EmptyNote.NoArchived
            isLoading.not() && noteList.key.isNotEmpty() && noteList.notes.isEmpty() -> EmptyNote.EmptyForSearch
            else -> EmptyNote.None
        }

    @Parcelize
    data class NoteActions(
        val copyToClipboard: String? = null,
        val shareNoteAsString: String? = null,
    ) : Parcelable

    enum class EmptyNote {
        None, EmptyForSearch, NoArchived
    }

}

