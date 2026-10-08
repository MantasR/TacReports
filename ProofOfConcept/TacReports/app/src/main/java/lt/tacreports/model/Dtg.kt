package lt.tacreports.model

import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Date-time group in the owner's format "DD HHMM Z MMM YY", e.g. "03 1825 C OCT 26".
 * Z is the NATO time-zone letter of the phone's current offset: in Lithuania C in summer (UTC+3)
 * and B in winter (UTC+2). Offsets that aren't whole hours fall back to UTC ("Z").
 */
object Dtg {
    private val MONTHS = arrayOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

    fun zoneLetter(offsetSeconds: Int): Char? {
        if (offsetSeconds % 3600 != 0) return null
        val h = offsetSeconds / 3600
        return when (h) {
            0 -> 'Z'
            in 1..9 -> 'A' + (h - 1)
            in 10..12 -> 'K' + (h - 10)
            in -12..-1 -> 'N' + (-h - 1)
            else -> null
        }
    }

    fun format(time: ZonedDateTime = ZonedDateTime.now()): String {
        var t = time
        var letter = zoneLetter(t.offset.totalSeconds)
        if (letter == null) {
            t = t.withZoneSameInstant(ZoneOffset.UTC)
            letter = 'Z'
        }
        return String.format(java.util.Locale.ROOT, "%02d %02d%02d %c %s %02d", t.dayOfMonth, t.hour, t.minute, letter, MONTHS[t.monthValue - 1], t.year % 100)
    }
}
