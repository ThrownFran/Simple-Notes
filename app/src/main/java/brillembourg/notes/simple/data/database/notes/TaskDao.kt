package brillembourg.notes.simple.data.database.notes

import androidx.room.*
import brillembourg.notes.simple.data.database.CategoryNoteCrossRef
import brillembourg.notes.simple.data.database.NoteWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TaskDao {

    //CREATE

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun create(item: NoteEntity): Long

    //GET

    @Query("SELECT * FROM taskentity WHERE is_archived = 0")
    abstract fun getList(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM taskentity WHERE is_archived = 1")
    abstract fun getArchivedList(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM taskentity")
    abstract suspend fun getListAsSuspend(): List<NoteEntity>

    //DELETE

    @Query("DELETE FROM taskentity WHERE note_id = :taskId")
    abstract suspend fun delete(taskId: Long)

    @Query("delete from taskentity where note_id in (:ids)")
    abstract suspend fun deleteTasks(ids: List<Long>)

    //UPDATE

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun save(task: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTasks(itemList: ArrayList<NoteEntity>)

    @Query("UPDATE taskentity SET `order` = :order WHERE note_id = :id")
    abstract suspend fun updateOrder(id: Long, order: Int)

    @Query("UPDATE taskentity SET `is_archived` = 1 WHERE note_id in (:ids)")
    abstract suspend fun archive(ids: List<Long>)

    @Query("UPDATE taskentity SET `is_archived` = 0 WHERE note_id in (:ids)")
    abstract suspend fun unarchive(ids: List<Long>)

    @Transaction
    @Query("SELECT * FROM taskentity WHERE is_archived = 0")
    abstract fun getNotesWithCategories(): Flow<List<NoteWithCategoriesEntity>>

//    @Transaction
//    @Query("SELECT * FROM taskentity WHERE is_archived = 0")
//    abstract fun getFilteredNotesWithCategories(ids: List<Long>): Flow<List<NoteWithCategoriesEntity>>

    @Transaction
    @Query("SELECT * FROM taskentity WHERE is_archived = 1")
    abstract fun getArchivedNotesWithCategories(): Flow<List<NoteWithCategoriesEntity>>

    @Transaction
    @Query("SELECT * FROM taskentity WHERE note_id = :noteId")
    abstract fun getNoteWithCategories(noteId: Long): Flow<NoteWithCategoriesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun createNoteCrossCategory(noteCrossRef: CategoryNoteCrossRef)

    @Delete
    abstract suspend fun deleteNoteCrossCategory(noteCrossRef: CategoryNoteCrossRef)

}