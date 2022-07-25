package brillembourg.notes.simple.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel

class TaskAdapter(
    val taskList: List<TaskPresentationModel>,
    val onClick: (TaskPresentationModel) -> Unit,
    val onLongClick: (TaskPresentationModel) -> Unit,
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            setupClickListeners()
            correctImageHeight()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener { onClick.invoke(taskList[adapterPosition]) }
            binding.root.setOnLongClickListener {
                onLongClick.invoke(taskList[adapterPosition])
                true
            }
        }

        private fun correctImageHeight() {
            val observer: ViewTreeObserver = binding.taskContraint.getViewTreeObserver()
            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.taskContraint.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    //Your code
                    val height = binding.taskContraint.measuredHeight
                    binding.taskImageBackground.layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, height +
                                    pxFromDp(binding.taskContraint.context, 4f).toInt()
                        )
                }
            })
        }

        fun bind(task: TaskPresentationModel) {

            bindTitle(task)


            binding.taskTextContent.text = task.content
            binding.taskTextDate.text = task.dateInLocal


        }

        private fun bindTitle(task: TaskPresentationModel) {
            with(binding.taskTextTitle) {
                isVisible = !task.title.isNullOrEmpty()
                text = task.title
            }
        }

        fun dpFromPx(context: Context, px: Float): Float {
            return px / context.getResources().getDisplayMetrics().density
        }

        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.getResources().getDisplayMetrics().density
        }

    }

}