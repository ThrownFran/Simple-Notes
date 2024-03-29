package brillembourg.notes.simple.data.database.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import brillembourg.notes.simple.domain.models.Category

@Entity(tableName = "categoryentity")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "category_id") var id: Long? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "order") val order: Int
)

fun CategoryEntity.toDomain(): Category {
    if (id == null) throw IllegalArgumentException("id is null")
    return Category(id!!, name, order)
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(id, name, order)
}