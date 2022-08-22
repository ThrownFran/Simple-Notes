package brillembourg.notes.simple.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TaskDao {

    //CREATE

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun create(item: TaskEntity): Long

    //GET

    @Query("SELECT * FROM taskentity WHERE is_archived = 0")
    abstract fun getList(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM taskentity WHERE is_archived = 1")
    abstract fun getArchivedList(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM taskentity")
    abstract suspend fun getListAsSuspend(): List<TaskEntity>

    //DELETE

    @Query("DELETE FROM taskentity WHERE id = :taskId")
    abstract suspend fun delete(taskId: Long)

    @Query("delete from taskentity where id in (:ids)")
    abstract suspend fun deleteTasks(ids: List<Long>)

    //UPDATE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun save(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTasks(itemList: ArrayList<TaskEntity>)

    @Query("UPDATE taskentity SET `order` = :order WHERE id = :id")
    abstract suspend fun updateOrder(id: Long, order: Int)

    @Query("UPDATE taskentity SET `is_archived` = 1 WHERE id in (:ids)")
    abstract suspend fun archive(ids: List<Long>)

    @Query("UPDATE taskentity SET `is_archived` = 0 WHERE id in (:ids)")
    abstract suspend fun unarchive(ids: List<Long>)


}