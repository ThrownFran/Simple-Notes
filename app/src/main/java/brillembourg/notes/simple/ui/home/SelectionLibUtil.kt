package brillembourg.notes.simple.ui.home

import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.R
import brillembourg.notes.simple.ui.models.TaskPresentationModel

fun TaskPresentationModel.bindTracker(
    idItem: Long,
    viewHolder: TaskAdapter.ViewHolder,
    tracker: SelectionTracker<Long>
) {
    if (tracker.isSelected(idItem)) {
        viewHolder.itemView.setBackgroundColor(
            ContextCompat.getColor(viewHolder.itemView.context, R.color.black)
        )
    } else {
        viewHolder.itemView.setBackgroundColor(0)
    }

}


fun TaskAdapter.buildTracker(recyclerView: RecyclerView, onSelectionChange: () -> Unit) {
    tracker = SelectionTracker.Builder<Long>(
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

class ItemsKeyProvider(private val adapter: TaskAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long =
        adapter.currentList[position].id

    override fun getPosition(key: Long): Int =
        adapter.currentList.indexOfFirst { it.id == key }
}

class MyItemDetailsLookup(
    private val recyclerView: RecyclerView,
    private val adapter: TaskAdapter
) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as TaskAdapter.ViewHolder)
                .getItemDetails(adapter = adapter)
        }
        return null
    }
}

fun TaskAdapter.ViewHolder.getItemDetails(adapter: TaskAdapter): ItemDetailsLookup.ItemDetails<Long> =
    object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = adapterPosition
        override fun getSelectionKey(): Long = adapter.currentList[adapterPosition].id
    }