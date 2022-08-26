package brillembourg.notes.simple.data.database.notes

import brillembourg.notes.simple.data.database.AppDatabase
import brillembourg.notes.simple.data.database.CategoryNoteCrossRef
import brillembourg.notes.simple.data.database.categories.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

class NoteDatabase(
    private val roomDatabase: AppDatabase
) {

    suspend fun createTask(
        content: String,
        dateCreated: String,
        title: String? = null
    ): NoteEntity {

        val lastOrderPosition = calculateLastOrderPosition()
        val nextOrderPosition = lastOrderPosition + 1

        return NoteEntity(null, content, dateCreated, title, nextOrderPosition).run {
            id = roomDatabase.taskDao().create(this)
            this
        }
    }

    private suspend fun calculateLastOrderPosition(): Int {
        val taskList = roomDatabase.taskDao().getListAsSuspend()
        var lastOrderPosition = 0
        taskList.filter { !it.isArchived }.forEach {
            if (it.order > lastOrderPosition) {
                lastOrderPosition = it.order
            }
        }
        return lastOrderPosition
    }

    suspend fun saveTask(task: NoteEntity) {
        roomDatabase.taskDao().save(task)
    }

    fun getArchivedTasks(): Flow<List<NoteEntity>> {
        return roomDatabase.taskDao().getArchivedList()
    }

    fun getTaskList(): Flow<List<NoteEntity>> {
        return roomDatabase.taskDao().getList()
    }

    suspend fun deleteTasks(ids: List<Long>) {
        return roomDatabase.taskDao().deleteTasks(ids)
    }

    suspend fun archiveTasks(ids: List<Long>) {
        return roomDatabase.taskDao().archive(ids)
    }

    suspend fun unArchiveTasks(ids: List<Long>) {
        val lastOrderPosition = calculateLastOrderPosition()
        val nextOrderPosition = lastOrderPosition + 1

        //New order for each task
        ids.forEachIndexed { index, id ->
            roomDatabase.taskDao().updateOrder(id, nextOrderPosition + index)
        }

        return roomDatabase.taskDao().unarchive(ids)
    }

    suspend fun deleteTask(taskId: Long) {
        roomDatabase.taskDao().delete(taskId)
    }

    suspend fun saveTasksReordering(taskList: List<NoteEntity>) {
        taskList.forEach {
            roomDatabase.taskDao().updateOrder(it.id!!, it.order)
        }
    }

    suspend fun saveTasks(taskList: List<NoteEntity>) {
        roomDatabase.taskDao().saveTasks(ArrayList(taskList))
    }

    suspend fun removeCategoryToNote(categoryEntity: CategoryEntity, noteEntity: NoteEntity) {
        roomDatabase.taskDao()
            .deleteNoteCrossCategory(CategoryNoteCrossRef(categoryEntity.id!!, noteEntity.id!!))
    }

    suspend fun addCategoryToNote(categoryEntity: CategoryEntity, noteEntity: NoteEntity) {
        roomDatabase.taskDao()
            .createNoteCrossCategory(CategoryNoteCrossRef(categoryEntity.id!!, noteEntity.id!!))
    }

    fun getCategoriesForNote(noteEntity: NoteEntity): Flow<List<CategoryEntity>> {
        return roomDatabase.taskDao().getNoteWithCategories(noteEntity.id!!)
            .transform {
                emit(it.categories)
            }
    }

}