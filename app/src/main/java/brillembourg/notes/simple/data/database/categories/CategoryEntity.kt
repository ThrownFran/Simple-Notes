package brillembourg.notes.simple.data.database.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import brillembourg.notes.simple.domain.models.Category

@Entity(tableName = "categoryentity")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = -1L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "order") val order: Int
)

fun CategoryEntity.toDomain(): Category {
    return Category(id, name, order)
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(id, name, order)
}