package brillembourg.notes.simple.presentation.home.renderers

import android.os.Parcelable
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.home.HeaderAdapter
import brillembourg.notes.simple.presentation.home.NoteList
import brillembourg.notes.simple.presentation.home.adapters.NoteAdapter
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.LayoutType
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.buildLayoutManager
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.getDragDirs

class NoteUiRenderer(
    private val recyclerView: RecyclerView,
    private var recyclerViewState: Parcelable?,
    private val onLayoutType: () -> LayoutType,
    private val onNavigateToCategories: () -> Unit,
    private val onSelection: () -> Unit,
    private val onNoteClick: (NotePresentationModel) -> Unit,
    private val onReorderedNotes: (tasks: List<NotePresentationModel>) -> Unit,
    private val onReorderedNotesCancelled: () -> Unit,
    private val onWizardVisibility: (Boolean) -> Unit
) {

    private fun getAdapter(): NoteAdapter? = try {
        getConcatAdapter()?.adapters?.first { it is NoteAdapter } as NoteAdapter
    } catch (e: Exception) {
        null
    }

    private fun getConcatAdapter() = (recyclerView.adapter as? ConcatAdapter?)

    fun render(noteList: NoteList) {
        if (noteList.mustRender) setupNoteList(noteList.notes)
    }

    private fun setupNoteList(taskList: List<NotePresentationModel>) {
        if (recyclerView.adapter == null) {
            setupTaskRecycler(taskList)
        } else {
            updateListAndNotify(getAdapter()!!, taskList)
        }

        onWizardVisibility(taskList.isEmpty())
//        binding.homeWizard.isVisible = taskList.isEmpty()
    }

    private fun setupTaskRecycler(taskList: List<NotePresentationModel>) {
        recyclerView.apply {
            val layoutType = onLayoutType.invoke()
            adapter = buildNoteAdapter(this, taskList, getDragDirs(layoutType))
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
        noteList: List<NotePresentationModel>,
        dragDirs: Int
    ): ConcatAdapter {

        val noteAdapter = NoteAdapter(
            dragDirs,
            recyclerView,
            onSelection = {
                onNoteSelection()
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
                submitList(noteList)
            }

        return ConcatAdapter(
            HeaderAdapter(emptyList<CategoryPresentationModel>().toMutableList()) {
                onNavigateToCategories()
            },
            noteAdapter
        )
    }


    private fun onNoteSelection() {
        onSelection()
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