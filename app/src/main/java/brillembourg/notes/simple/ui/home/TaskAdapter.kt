package brillembourg.notes.simple.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.ui.TaskPresentationModel

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
            binding.root.setOnClickListener { onClick.invoke(taskList[adapterPosition]) }
            binding.root.setOnLongClickListener {
                onLongClick.invoke(taskList[adapterPosition])
                true
            }
        }

        fun bind(task: TaskPresentationModel) {
            binding.taskTextContent.text = task.content
            binding.taskTextDate.text = task.dateInLocal
        }

    }

}