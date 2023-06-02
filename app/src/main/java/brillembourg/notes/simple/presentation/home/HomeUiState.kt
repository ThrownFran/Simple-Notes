package brillembourg.notes.simple.presentation.home

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.trash.SelectionModeActive
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUiState(
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive = SelectionModeActive(),
    val noteActions: NoteActions = NoteActions(),
    val selectCategoriesState: SelectCategoriesState = SelectCategoriesState(),
    val noteList: NoteList = NoteList(),
    val isLoading: Boolean = noteList.hasLoaded.not()
) : Parcelable {

    val emptyNotesState: EmptyNote
        get() = when {
            isLoading -> EmptyNote.None
            noteList.key.isEmpty()
                    && noteList.notes.isEmpty()
                    && noteList.filteredCategories.isEmpty() -> EmptyNote.Wizard

            noteList.key.isEmpty()
                    && noteList.notes.isEmpty()
                    && noteList.filteredCategories.size == 1 -> EmptyNote.EmptyForLabel

            noteList.key.isEmpty()
                    && noteList.notes.isEmpty()
                    && noteList.filteredCategories.size > 1 -> EmptyNote.EmptyForMultipleLabels

            noteList.key.isNotEmpty()
                    && noteList.notes.isEmpty() -> EmptyNote.EmptyForSearch

            else -> EmptyNote.None
        }

    enum class EmptyNote {
        Wizard, EmptyForLabel, EmptyForSearch, EmptyForMultipleLabels, None
    }
}


@Parcelize
data class NoteList(
    val notes: List<NotePresentationModel> = ArrayList(),
    val filteredCategories: List<CategoryPresentationModel> = emptyList(),
    val mustRender: Boolean = false, //To avoid rendering set false
    val hasLoaded: Boolean = false, //To avoid rendering set false
    val key: String = "",
) : Parcelable

@Parcelize
data class NoteActions(
    val copyToClipboard: String? = null,
    val shareNoteAsString: String? = null,
) : Parcelable

@Parcelize
data class SelectCategoriesState(
    val isFilterCategoryMenuAvailable: Boolean = false,
    val navigate: Boolean = false,
    val isShowing: Boolean = false
) : Parcelable