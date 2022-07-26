package brillembourg.notes.simple.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import brillembourg.notes.simple.domain.models.Task

@Entity(tableName = "taskentity")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "description") val content: String,
    @ColumnInfo(name = "date_created") val dateCreated: String,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "order") val order: Int
)

fun TaskEntity.toDomain(): Task {
    if (id == null) throw IllegalArgumentException("id is null")
    return Task(
        id = id!!,
        title = title,
        content = content,
        order = order,
        date = dateCreated
    )
}

fun Task.toData(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        content = content,
        order = order,
        dateCreated = date
    )