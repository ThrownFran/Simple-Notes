package brillembourg.notes.simple.data

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

interface DateProvider {
    fun getCurrentTime(): String
    fun formatTimeToLocalDate(currentTime: String): String
    fun formatLocalDateToTime(localDate: String): String
    fun formatLocalDateToFriendlyFormat(localDate: String): String
}

class DateProviderImp : DateProvider {

    private val dateFormat = "dd-MM-yyyy HH:mm:ss"
    private val friendlyFormat = "E MMM dd yyyy"

    override fun getCurrentTime(): String = Instant.now().toString()

    override fun formatLocalDateToFriendlyFormat(localDate: String): String {
        val localDateTime =
            LocalDateTime.ofInstant(Instant.parse(localDate), ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.ofPattern(friendlyFormat))
    }

    override fun formatTimeToLocalDate(currentTime: String): String {
        val instant = Instant.parse(currentTime)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.ofPattern(dateFormat))
    }

    override fun formatLocalDateToTime(localDate: String): String {
        val formatter = DateTimeFormatter.ofPattern(dateFormat)
        val instant =
            LocalDateTime.parse(localDate, formatter).atZone(ZoneId.systemDefault()).toInstant()
        return instant.toString()
    }


}