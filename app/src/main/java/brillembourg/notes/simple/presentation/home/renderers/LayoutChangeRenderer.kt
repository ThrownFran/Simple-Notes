package brillembourg.notes.simple.presentation.home.renderers

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.home.adapters.NoteAdapter
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.LayoutType
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.changeLayout
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.getDragDirs
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.toLayoutType

class LayoutChangeRenderer(
    private val recyclerView: RecyclerView,
    private val onLayoutChange: (NoteLayout) -> Unit
) {

    fun render(noteLayout: NoteLayout) {
        onChangeLayout(
            recyclerView,
            noteLayout.toLayoutType()
        )
    }

    private fun onChangeLayout(
        recyclerView: RecyclerView,
        layoutType: LayoutType
    ) {
        val noteAdapter = getAdapter() ?: return

        changeLayout(
            recyclerView,
            layoutType,
            noteAdapter.currentList
        )

        noteAdapter.setDragDirections(recyclerView, getDragDirs(layoutType))
    }

    fun onClickStaggeredLayout() {
        onLayoutChange(NoteLayout.Grid)
    }

    fun onClickVerticalLayout() {
        onLayoutChange(NoteLayout.Vertical)
    }

    private fun getAdapter(): NoteAdapter? = try {
        getConcatAdapter()?.adapters?.first { it is NoteAdapter } as NoteAdapter
    } catch (e: Exception) {
        null
    }

    private fun getConcatAdapter() = (recyclerView.adapter as? ConcatAdapter?)

}