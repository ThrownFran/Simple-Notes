package brillembourg.notes.simple.presentation.home

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.trash.SelectionModeActive
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUiState(
    val noteList: NoteList = NoteList(),
    val noteLayout: NoteLayout = NoteLayout.Vertical,
    val selectionModeActive: SelectionModeActive? = null,

    val copyToClipboard: String? = null,
    val shareNoteAsString: String? = null,

    val filteredCategories: List<CategoryPresentationModel> = emptyList(),
    val selectFilterCategories: SelectFilterCategories = SelectFilterCategories()
) : Parcelable

@Parcelize
data class NoteList(
    val notes: List<NotePresentationModel> = ArrayList(),
    val mustRender: Boolean = false //To avoid rendering set false
) : Parcelable

@Parcelize
data class SelectFilterCategories(
    val isFilterCategoryMenuAvailable: Boolean = false,
    val navigate: Boolean = false,
    val isShowing: Boolean = false,
    val categories: List<CategoryPresentationModel> = emptyList()
) : Parcelable