package brillembourg.notes.simple.data.database.categories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao {

    //CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun create(item: CategoryEntity): Long

    //GET
    @Query("SELECT * FROM categoryentity")
    abstract fun getList(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categoryentity")
    abstract suspend fun getListAsSuspend(): List<CategoryEntity>

    //DELETE
    @Query("delete from categoryentity where id in (:ids)")
    abstract suspend fun deleteMultiple(ids: List<Long>)

    //UPDATE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun save(categoryEntity: CategoryEntity)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract suspend fun saveMultiple(itemList: ArrayList<CategoryEntity>)

    @Query("UPDATE categoryentity SET `order` = :order WHERE id = :id")
    abstract suspend fun updateOrder(id: Long, order: Int)


}