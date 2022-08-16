package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.AppDatabase
import brillembourg.notes.simple.data.room.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskDatabase(
    private val roomDatabase: AppDatabase
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

    suspend fun saveTask(task: TaskEntity) {
        roomDatabase.taskDao().save(task)
    }

    fun getArchivedTasks(): Flow<List<TaskEntity>> {
        return roomDatabase.taskDao().getArchivedList()
    }

    fun getTaskList(): Flow<List<TaskEntity>> {
        return roomDatabase.taskDao().getList()
    }

    suspend fun deleteTasks(ids: List<Long>) {
        return roomDatabase.taskDao().deleteTasks(ids)
    }

    suspend fun archiveTasks(ids: List<Long>) {
        return roomDatabase.taskDao().archive(ids)
    }

    suspend fun deleteTask(taskId: Long) {
        roomDatabase.taskDao().delete(taskId)
    }

    suspend fun saveTasksReordering(taskList: List<TaskEntity>) {
        taskList.forEach {
            roomDatabase.taskDao().updateOrder(it.id!!, it.order)
        }
    }

    suspend fun saveTasks(taskList: List<TaskEntity>) {
        roomDatabase.taskDao().saveTasks(ArrayList(taskList))
    }

}