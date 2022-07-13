package brillembourg.notes.simple.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface DateProvider {
    fun getCurrentTime(): String
    fun formatTimeToLocalDate(currentTime: String): String
    fun formatLocalDateToTime(localDate: String) : String
}

class DateProviderImp : DateProvider {

    private val PATTERN = "dd-MM-yyyy HH:mm:ss"

    override fun getCurrentTime(): String = Instant.now().toString()

    override fun formatTimeToLocalDate(currentTime: String): String {
        val instant = Instant.parse(currentTime)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.ofPattern(PATTERN))
    }

    override fun formatLocalDateToTime(localDate: String): String {
        val formatter = DateTimeFormatter.ofPattern(PATTERN)
        val instant = LocalDateTime.parse(localDate,formatter).atZone(ZoneId.systemDefault()).toInstant()
        return instant.toString()
    }


}