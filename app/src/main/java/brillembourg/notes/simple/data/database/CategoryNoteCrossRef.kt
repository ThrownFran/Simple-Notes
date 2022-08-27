package brillembourg.notes.simple.data.database

import androidx.room.*
import brillembourg.notes.simple.data.database.categories.CategoryEntity
import brillembourg.notes.simple.data.database.categories.toDomain
import brillembourg.notes.simple.data.database.notes.NoteEntity
import brillembourg.notes.simple.data.database.notes.toDomain
import brillembourg.notes.simple.domain.models.NoteWithCategories


@Entity(tableName = "category_note_cross_ref", primaryKeys = ["category_id", "note_id"])
class CategoryNoteCrossRef(
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "note_id") val noteId: Long
)

data class CategoryWithNotes(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "note_id",
        associateBy = Junction(CategoryNoteCrossRef::class)
    )
    val notes: List<NoteEntity>
)

data class NoteWithCategoriesEntity(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "note_id",
        entityColumn = "category_id",
        associateBy = Junction(CategoryNoteCrossRef::class)
    )
    val categories: List<CategoryEntity>
)

fun NoteWithCategoriesEntity.toDomain(): NoteWithCategories {
    return NoteWithCategories(note.toDomain(), categories.map { it.toDomain() })
}