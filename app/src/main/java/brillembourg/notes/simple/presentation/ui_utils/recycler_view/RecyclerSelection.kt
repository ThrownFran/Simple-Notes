package brillembourg.notes.simple.presentation.ui_utils

import brillembourg.notes.simple.presentation.models.IsSelectable

/**
 * Track selectable items in a ViewHolder {@see IsSelectable}
 */
interface Selectable<T : IsSelectable> {

    /**
     * Setup method and callbacks
     */
    fun setupSelection(
        bindSelection: (T) -> Unit, //Use to bind selected or unselected item
        onClickWithNoSelection: ((T) -> Unit)?, //Normal click item with disabled selection
        onSelected: ((isSelected: Boolean, id: Long) -> Unit)?, //When selecting or unselecting item
        onReadyToDrag: (() -> Unit)? //Start drag suggestion
    )

    /**
     * Use this method in your view click listener
     */
    fun onItemClick(position: Int, t: T)

    /**
     * Use this method to activate selection, normally in long click listener
     */
    fun onItemSelection(position: Int, t: T)
}

class SelectableImp<T : IsSelectable>(
    val getCurrentList: () -> List<T>,
) : Selectable<T> {

    lateinit var bindSelection: ((T) -> Unit)
    private var onClickWithNoSelection: ((T) -> Unit)? = null
    private var onSelected: ((isSelected: Boolean, id: Long) -> Unit)? = null
    private var onReadyToDrag: (() -> Unit)? = null

    override fun setupSelection(
        bindSelection: (T) -> Unit,
        onClickWithNoSelection: ((T) -> Unit)?,
        onSelected: ((isSelected: Boolean, id: Long) -> Unit)?,
        onReadyToDrag: (() -> Unit)?
    ) {
        this.bindSelection = bindSelection
        this.onSelected = onSelected
        this.onClickWithNoSelection = onClickWithNoSelection
        this.onReadyToDrag = onReadyToDrag
    }

    override fun onItemClick(position: Int, t: T) {
        if (isSelectionVisible()) {
            clickInSelectionVisible(position, t)
            return
        }
        clickInNormalState(position)
    }

    override fun onItemSelection(
        position: Int,
        t: T,
    ) {
        if (isSelectionNotVisible()) {
            onReadyToDrag?.invoke()
            toggleItemSelection(position, t)
        } else {
            longClickInSelectionVisible(position, t)
        }
    }

    private fun longClickInSelectionVisible(position: Int, t: T) {
        toggleItemSelection(position, t)
    }

    private fun isSelectionVisible(): Boolean =
        getCurrentList().any { it.isSelected }

    private fun clickInSelectionVisible(position: Int, t: T) {
        toggleItemSelection(position, t)
    }

    private fun clickInNormalState(position: Int) {
        onClickWithNoSelection?.invoke(getCurrentList()[position])
    }

    private fun toggleItemSelection(position: Int, t: T) {
        t.let {
            it.isSelected = !it.isSelected
            bindSelection(it)
            onSelected?.invoke(it.isSelected, it.id)
        }
    }

    private fun isSelectionNotVisible() = !isSelectionVisible()


}