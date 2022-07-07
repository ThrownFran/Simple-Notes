package brillembourg.notes.simple.ui

import android.os.Parcelable
import brillembourg.notes.simple.domain.models.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskPresentationModel (val id: Long, var content: String, val date: String): Parcelable