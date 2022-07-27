package brillembourg.notes.simple.data

import android.content.Context
import brillembourg.notes.simple.data.room.AppDatabase
import brillembourg.notes.simple.data.room.TaskEntity
import brillembourg.notes.simple.data.room.toData
import brillembourg.notes.simple.domain.models.Task
import kotlinx.coroutines.flow.Flow

class TaskDatabase(
    val context: Context,
    val roomDatabase: AppDatabase
) {

    suspend fun createTask(
        content: String,
        dateCreated: String,
        title: String? = null
    ): TaskEntity {

        val lastOrderPosition = calculateLastOrderPosition()
        val nextOrderPosition = lastOrderPosition + 1

        return TaskEntity(null, content, dateCreated, title, nextOrderPosition).run {
            id = roomDatabase.taskDao().create(this)
            this
        }
    }

    private suspend fun calculateLastOrderPosition(): Int {
        val taskList = roomDatabase.taskDao().getListAsSuspend()
        var lastOrderPosition = 0
        taskList.forEach {
            if (it.order > lastOrderPosition) {
                lastOrderPosition = it.order
            }
        }
        return lastOrderPosition
    }

    suspend fun saveTask(task: Task) {
        roomDatabase.taskDao().save(task.toData())
    }

    fun getTaskList(): Flow<List<TaskEntity>> {
        return roomDatabase.taskDao().getList()
    }

    suspend fun deleteTask(taskId: Long) {
        roomDatabase.taskDao().delete(taskId)
    }

}