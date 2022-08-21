package brillembourg.notes.simple.domain.models

data class Note(
    val id: Long,
    var title: String? = null,
    var content: String,
    val date: String,
    val order: Int,
    val isArchived: Boolean = false
)