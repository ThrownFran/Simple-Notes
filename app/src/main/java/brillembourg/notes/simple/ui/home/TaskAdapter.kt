package brillembourg.notes.simple.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemTaskBinding
import brillembourg.notes.simple.domain.models.Task

class TaskAdapter(val taskList: List<Task>, val onClick: (Task) -> Unit) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    inner class ViewHolder (val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onClick.invoke(taskList[adapterPosition]) }
        }

        fun bind (task: Task) {
            binding.taskTextContent.text = task.content
        }

    }

}