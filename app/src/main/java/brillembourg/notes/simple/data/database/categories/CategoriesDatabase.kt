package brillembourg.notes.simple.data.database.categories

import brillembourg.notes.simple.data.database.AppDatabase
import kotlinx.coroutines.flow.Flow

class CategoriesDatabase(
    private val roomDatabase: AppDatabase
) {

    fun getList(): Flow<List<CategoryEntity>> {
        return roomDatabase.categoryDao().getList()
    }

    suspend fun create(
        name: String,
    ): CategoryEntity {

        val lastOrderPosition = calculateLastOrderPosition()
        val nextOrderPosition = lastOrderPosition + 1

        return CategoryEntity(null, name, nextOrderPosition).run {
            id = roomDatabase.categoryDao().create(this)
            this
        }
    }

    private suspend fun calculateLastOrderPosition(): Int {
        val taskList = roomDatabase.categoryDao().getListAsSuspend()
        var lastOrderPosition = 0
        taskList.forEach {
            if (it.order > lastOrderPosition) {
                lastOrderPosition = it.order
            }
        }
        return lastOrderPosition
    }

    suspend fun save(categoryEntity: CategoryEntity) {
        roomDatabase.categoryDao().save(categoryEntity)
    }

    suspend fun deleteMultiple(ids: List<Long>) {
        return roomDatabase.categoryDao().deleteMultiple(ids)
    }

    suspend fun saveReordering(taskList: List<CategoryEntity>) {
        taskList.forEach {
            roomDatabase.categoryDao().updateOrder(it.id!!, it.order)
        }
    }

}