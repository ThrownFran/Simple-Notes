package brillembourg.notes.simple.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TaskDao {

    @Query("SELECT * FROM taskentity")
    abstract fun getList(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM taskentity")
    abstract suspend fun getListAsSuspend(): List<TaskEntity>

    @Query("DELETE FROM taskentity WHERE id = :taskId")
    abstract suspend fun delete(taskId: Long)

    @Query("delete from taskentity where id in (:ids)")
    abstract suspend fun deleteTasks(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun save(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTasks(itemList: ArrayList<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun create(item: TaskEntity): Long


}