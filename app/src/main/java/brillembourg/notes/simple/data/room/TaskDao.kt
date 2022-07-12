package brillembourg.notes.simple.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class TaskDao {

    @Query("SELECT * FROM taskentity")
    abstract suspend fun getTaskList(): List<TaskEntity>

    @Query("DELETE FROM taskentity WHERE id = :taskId")
    abstract suspend fun deleteTask(taskId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveCurrentWeather(task: TaskEntity)



}