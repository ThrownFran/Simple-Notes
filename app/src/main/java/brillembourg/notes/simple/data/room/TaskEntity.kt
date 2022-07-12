package brillembourg.notes.simple.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import brillembourg.notes.simple.domain.models.Task

@Entity(primaryKeys = ["id"])
data class TaskEntity(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "description") val content: String,
    @ColumnInfo(name = "date_created") val dateCreated: String
)

fun TaskEntity.toDomain(): Task {
    return Task(id.toLong(), content, dateCreated)
}

fun TaskEntity.fromDomain(task: Task): TaskEntity =
    TaskEntity(
        task.id.toString(),
        task.content, task.date
    )