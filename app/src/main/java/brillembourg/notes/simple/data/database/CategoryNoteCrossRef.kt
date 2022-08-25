package brillembourg.notes.simple.data.database

import androidx.room.*
import brillembourg.notes.simple.data.database.categories.CategoryEntity
import brillembourg.notes.simple.data.database.notes.NoteEntity


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

data class NoteWithCategories(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "note_id",
        entityColumn = "category_id",
        associateBy = Junction(CategoryNoteCrossRef::class)
    )
    val categories: List<CategoryEntity>
)