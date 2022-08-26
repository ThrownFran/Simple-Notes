package brillembourg.notes.simple.data.database.notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import brillembourg.notes.simple.domain.models.Note

@Entity(tableName = "taskentity")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "note_id") var id: Long? = null,
    @ColumnInfo(name = "description") val content: String,
    @ColumnInfo(name = "date_created") val dateCreated: String,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "order") val order: Int,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false
)

fun NoteEntity.toDomain(): Note {
    if (id == null) throw IllegalArgumentException("id is null")
    return Note(
        id = id!!,
        title = title,
        content = content,
        order = order,
        date = dateCreated,
        isArchived = isArchived
    )
}

fun Note.toEntity(): NoteEntity =
    NoteEntity(
        id = id,
        title = title,
        content = content,
        order = order,
        dateCreated = date,
        isArchived = isArchived
    )