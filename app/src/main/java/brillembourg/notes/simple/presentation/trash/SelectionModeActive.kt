package brillembourg.notes.simple.presentation.trash

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*Notes are selected and contextual bar is shown*/
@Parcelize
data class SelectionModeActive(
    val size: Int
) : Parcelable