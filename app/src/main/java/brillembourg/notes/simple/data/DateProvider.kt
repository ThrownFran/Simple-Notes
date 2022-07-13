package brillembourg.notes.simple.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface DateProvider {
    fun getCurrentTime(): String
    fun formatTimeToLocalDate(currentTime: String): String
    fun formatLocalDateToTime(localDate: String) : String
}

class DateProviderImp : DateProvider {

    val PATTERN = "yyyy.MM.dd G 'at' HH:mm:ss z"

    override fun getCurrentTime(): String = Instant.now().toString()

    override fun formatTimeToLocalDate(currentTime: String): String {
        val instant = Instant.parse(currentTime)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        localDateTime.format(DateTimeFormatter.ofPattern(PATTERN))
        return localDateTime.toString()
    }

    override fun formatLocalDateToTime(localDate: String): String {
        val formatter = DateTimeFormatter.ofPattern(PATTERN)
        val instant = LocalDateTime.parse(localDate,formatter).atZone(ZoneId.systemDefault()).toInstant()
        return instant.toString()
    }


}