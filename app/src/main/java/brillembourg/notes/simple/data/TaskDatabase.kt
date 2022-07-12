package brillembourg.notes.simple.data

import android.content.Context
import brillembourg.notes.simple.data.room.AppDatabase
import brillembourg.notes.simple.data.room.TaskEntity
import brillembourg.notes.simple.data.room.toData
import brillembourg.notes.simple.domain.models.Task

class TaskDatabase(
    val context: Context,
    val roomDatabase: AppDatabase
) {

    suspend fun createTask(content: String): TaskEntity {
        val dateCreated = ""
        return TaskEntity(null,content,dateCreated).run {
            id = roomDatabase.taskDao().create(this)
            this
        }
    }

    suspend fun saveTask (task: Task) {
        roomDatabase.taskDao().save(task.toData())
    }

    suspend fun getTaskList() : List<TaskEntity> {
        return roomDatabase.taskDao().getList()
    }

    suspend fun deleteTask(taskId: Long) {
        roomDatabase.taskDao().delete(taskId)
    }

}