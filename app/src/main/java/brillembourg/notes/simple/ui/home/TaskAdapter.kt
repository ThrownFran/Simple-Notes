package brillembourg.notes.simple.ui.home

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel


class TaskAdapter(
    dragAndDropDirs: Int,
    val recyclerView: RecyclerView,
    val onClick: (TaskPresentationModel) -> Unit,
    val onSelection: () -> Unit,
    val onReorderSuccess: (List<TaskPresentationModel>) -> Unit,
    val onReorderCanceled: () -> Unit
) : ListAdapter<TaskPresentationModel, TaskAdapter.ViewHolder>(DiffCallback) {

    var dragAndDrogList: List<TaskPresentationModel>? = null
    var isDragging = false
    var tracker: SelectionTracker<Long>? = null
    var currentPosition: Int? = null

    var itemTouchHelper = setupDragAndDropTouchHelper(dragAndDropDirs)

    override fun submitList(list: List<TaskPresentationModel>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            setupClickListeners()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener { click() }

            binding.root.setOnLongClickListener {
                currentPosition = adapterPosition

                if (isSelectionNotVisible()) {
                    longClickInNormalState()
                } else {
                    longClickInSelectionVisible()
                }
                true
            }
        }

        private fun longClickInSelectionVisible() {
            toggleItemSelection()
        }

        private fun longClickInNormalState() {
            startDrag()
            toggleItemSelection()
        }

        private fun click() {
            if (isSelectionVisible()) {
                clickInSelectionVisible()
                return
            }
            clickInNormalState()
        }

        private fun clickInNormalState() {
            onClick.invoke(getItem(adapterPosition))
        }

        private fun clickInSelectionVisible() {
            toggleItemSelection()
            if (isSelectionVisible()) {
                disableDragNDrop()
            }
        }

        fun bind(task: TaskPresentationModel) {
            bindTitle(task)
            bindContent(task)
            bindDate(task)
            bindSelection(task)
        }

        private fun bindDate(task: TaskPresentationModel) {
            binding.taskTextDate.text = task.dateInLocal
        }

        private fun bindContent(task: TaskPresentationModel) {
//            binding.taskTextContent.text = "${task.order}. ${task.content}"
            binding.taskTextContent.text = task.content
        }

        fun bindSelection(task: TaskPresentationModel) {
            if (task.isSelected) {
                setBackgroundSelected()
            } else {
                setBackgroundTransparent()
            }
        }

        fun setBackgroundTransparent() {
            itemView.setBackgroundColor(
                ContextCompat.getColor(itemView.context, R.color.transparent)
            )
        }

        fun setBackgroundSelected() {
            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorSecondaryVariant,
                typedValue,
                true
            )
            val color = typedValue.data
            itemView.setBackgroundColor(color)
        }

        private fun bindTitle(task: TaskPresentationModel) {
            with(binding.taskTextTitle) {
                isVisible = !task.title.isNullOrEmpty()
                text = task.title
            }
        }

        private fun isSelectionNotVisible() = !isSelectionVisible()

        private fun toggleItemSelection() {
            getItem(adapterPosition)?.let {
                it.isSelected = !it.isSelected
                bindSelection(it)
                onSelection.invoke()
            }
        }

        fun disableDragNDrop() {
            itemTouchHelper.attachToRecyclerView(null)
//            itemView.setOnTouchListener(null)
        }

        private fun isSelectionVisible(): Boolean =
            currentList.any { it.isSelected }

        private fun startDrag() {
            enableDragNDrop()
            itemTouchHelper.startDrag(this@ViewHolder)
//            var startX: Float = 0f
//            var startY: Float = 0f
//            itemView.setOnTouchListener(object : View.OnTouchListener {
//                override fun onTouch(view: View, event: MotionEvent): Boolean {
//
//                    // Check for drag gestures
//                    when (event.getAction()) {
//                        MotionEvent.ACTION_DOWN -> {
//                            startX = event.getX()
//                            startY = event.getY()
//                        }
//                        MotionEvent.ACTION_UP -> {
//                        }
//                        MotionEvent.ACTION_MOVE -> {
//                            val translateX = event.getX() - startX;
//                            val translateY = event.getY() - startY;
//                            if(translateX > 1000 || translateY > 1000) {
//                                Log.e("Task adapter: Translate Y", translateY.toString())
//                                Log.e("Task adapter: Translate X", translateX.toString())
////                                enableDragNDrop()
////                                itemTouchHelper.startDrag(this@ViewHolder)
//                            }
//                        }
//                    }
//                    return true
//                }
//            })
        }

        private fun enableDragNDrop() {
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }


        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.getResources().getDisplayMetrics().density
        }

//        private fun correctImageHeight() {
//            val observer: ViewTreeObserver = binding.taskContraint.viewTreeObserver
//            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    binding.taskContraint.viewTreeObserver.removeGlobalOnLayoutListener(this)
//                    //Your code
//                    val height = binding.taskContraint.measuredHeight
//                    binding.taskImageBackground.layoutParams =
//                        FrameLayout.LayoutParams(
//                            FrameLayout.LayoutParams.MATCH_PARENT, height +
//                                    4f.fromDpToPixel(context = itemView.context).toInt()
//                        )
//                    binding.taskImageBackground.isVisible = true
////                    itemView.invalidate()
//                }
//            })
//        }


    }


    companion object {
        private val DiffCallback = setupDiffCallback()

        private fun setupDiffCallback() = object : DiffUtil.ItemCallback<TaskPresentationModel>() {
            override fun areItemsTheSame(
                oldItem: TaskPresentationModel,
                newItem: TaskPresentationModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: TaskPresentationModel,
                newItem: TaskPresentationModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

