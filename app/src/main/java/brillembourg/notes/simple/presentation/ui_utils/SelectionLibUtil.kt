package brillembourg.notes.simple.presentation.ui_utils

import android.view.MotionEvent
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.presentation.home.NoteAdapter
import brillembourg.notes.simple.presentation.home.NoteViewHolder
import brillembourg.notes.simple.presentation.models.TaskPresentationModel


fun TaskPresentationModel.bindTracker(
    idItem: Long,
    viewHolder: NoteViewHolder,
    tracker: SelectionTracker<Long>
) {
    if (tracker.isSelected(idItem)) {
        viewHolder.itemView.setBackgroundColor(
            0
//            ContextCompat.getColor(viewHolder.itemView.context, R.color.black)
        )
    } else {
        viewHolder.itemView.setBackgroundColor(0)
    }

}


fun NoteAdapter.buildTracker(recyclerView: RecyclerView, onSelectionChange: () -> Unit) {
    //Tracker in Adapter
    SelectionTracker.Builder<Long>(
        "mySelection",
        recyclerView,
        ItemsKeyProvider(this),
        MyItemDetailsLookup(recyclerView, this),
        StorageStrategy.createLongStorage()
    ).withSelectionPredicate(
        SelectionPredicates.createSelectAnything()
    ).build().also {
        it.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    onSelectionChange.invoke()
                }
            })
    }
}

class ItemsKeyProvider(private val adapter: NoteAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long =
        adapter.currentList[position].id

    override fun getPosition(key: Long): Int =
        adapter.currentList.indexOfFirst { it.id == key }
}

class MyItemDetailsLookup(
    private val recyclerView: RecyclerView,
    private val adapter: NoteAdapter
) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as NoteViewHolder)
                .getItemDetails(adapter = adapter)
        }
        return null
    }
}

fun NoteViewHolder.getItemDetails(adapter: NoteAdapter): ItemDetailsLookup.ItemDetails<Long> =
    object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition
        override fun getSelectionKey(): Long = adapter.currentList[adapterPosition].id
    }