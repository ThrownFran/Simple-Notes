package brillembourg.notes.simple.presentation.home.renderers

import android.os.Parcelable
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import brillembourg.notes.simple.presentation.home.HeaderAdapter
import brillembourg.notes.simple.presentation.home.NoteList
import brillembourg.notes.simple.presentation.home.adapters.NoteAdapter
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.LayoutType
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.buildLayoutManager
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.getDragDirs

class NoteUiRenderer(
    private val recyclerView: RecyclerView,
    private val isDragEnabled: Boolean = false,
    private var recyclerViewState: Parcelable?,
    private val onLayoutType: () -> LayoutType,
    private val onNavigateToCategories: () -> Unit,
    private val onSelection: (isSelected: Boolean, id: Long) -> Unit,
    private val onNoteClick: (NotePresentationModel) -> Unit,
    private val onReorderedNotes: (tasks: List<NotePresentationModel>) -> Unit,
    private val onReorderedNotesCancelled: () -> Unit
) {

    private fun getAdapter(): NoteAdapter? = try {
        getConcatAdapter()?.adapters?.first { it is NoteAdapter } as NoteAdapter
    } catch (e: Exception) {
        null
    }

    private fun getConcatAdapter() = (recyclerView.adapter as? ConcatAdapter?)

    fun render(noteList: NoteList) {
        if (noteList.mustRender) setupNoteList(noteList)
        filteredCategoriesState(noteList.filteredCategories)
    }

    private fun getHeaderAdapter() =
        getConcatAdapter()?.adapters?.filterIsInstance<HeaderAdapter>()?.firstOrNull()

    private fun filteredCategoriesState(filteredCategories: List<CategoryPresentationModel>) {

        val headerAdapter = getHeaderAdapter()

        if (filteredCategories.isEmpty()) {
            headerAdapter?.let { getConcatAdapter()?.removeAdapter(it) }
            return
        }

        if (headerAdapter?.filteredCategories?.size == filteredCategories.size) return

        if (headerAdapter != null) {
            headerAdapter.filteredCategories.clear()
            headerAdapter.filteredCategories.addAll(filteredCategories)
            headerAdapter.notifyItemChanged(0)
        } else {
            (recyclerView.adapter as? ConcatAdapter?)?.addAdapter(
                0,
                HeaderAdapter(filteredCategories.toDiplayOrder().toMutableList()) {
                    onNavigateToCategories()
                })
        }
    }

    private fun setupNoteList(noteList: NoteList) {
        if (recyclerView.adapter == null) {
            setupTaskRecycler(noteList)
        } else {
            updateListAndNotify(getAdapter()!!, noteList.notes)
        }
    }

    private fun setupTaskRecycler(noteList: NoteList) {
        recyclerView.apply {
            val layoutType = onLayoutType.invoke()
            adapter = buildNoteAdapter(this, noteList, getDragDirs(layoutType))
            layoutManager = buildLayoutManager(context, layoutType).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun updateListAndNotify(
        noteAdapter: NoteAdapter,
        taskList: List<NotePresentationModel>
    ) {
        submitListAndScrollIfApplies(noteAdapter, noteAdapter.currentList, taskList)
    }

    private fun submitListAndScrollIfApplies(
        noteAdapter: NoteAdapter,
        currentList: List<NotePresentationModel>,
        taskList: List<NotePresentationModel>
    ) {
        val isInsertingInList = currentList.size < taskList.size
        noteAdapter.submitList(taskList) { if (isInsertingInList) scrollToTop() }
    }

    private fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    private fun buildNoteAdapter(
        recyclerView: RecyclerView,
        noteList: NoteList,
        dragDirs: Int
    ): ConcatAdapter {

        val noteAdapter = NoteAdapter(
            dragAndDropDirs = dragDirs,
            recyclerView = recyclerView,
            isDragEnabled = isDragEnabled,
            onSelection = { isSelected, id ->
                onNoteSelection(isSelected, id)
            },
            onClick = { task ->
                onNoteClicked(task)
            },
            onReorderSuccess = { tasks ->
                onReorderedNotes(tasks)
            },
            onReorderCanceled = {
                onReorderNotesCancelled()
            })
            .apply {
                submitList(noteList.notes)
            }
        return ConcatAdapter(
            HeaderAdapter(noteList.filteredCategories.toMutableList()) {
                onNavigateToCategories()
            },
            noteAdapter
        )
    }


    private fun onNoteSelection(isSelected: Boolean, id: Long) {
        onSelection(isSelected, id)
    }

    private fun onNoteClicked(it: NotePresentationModel) {
        onNoteClick(it)
    }

    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recyclerViewState.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun onReorderedNotes(tasks: List<NotePresentationModel>) {
        onReorderedNotes.invoke(tasks)
    }

    private fun onReorderNotesCancelled() {
        onReorderedNotesCancelled()
    }

    fun saveRecyclerState() {
        recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
    }

}