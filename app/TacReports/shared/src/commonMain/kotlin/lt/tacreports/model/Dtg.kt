package lt.tacreports.model

import lt.tacreports.nowMillis
import lt.tacreports.utcOffsetSeconds

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

    fun format(epochMillis: Long = nowMillis(), offsetSeconds: Int = utcOffsetSeconds(epochMillis)): String {
        var offset = offsetSeconds
        val letter = zoneLetter(offset) ?: 'Z'.also { offset = 0 }
        val local = epochMillis.floorDiv(1000L) + offset
        val (year, month, day) = civil(local.floorDiv(86_400L))
        val secOfDay = local.mod(86_400L).toInt()
        return "${two(day)} ${two(secOfDay / 3600)}${two(secOfDay / 60 % 60)} $letter ${MONTHS[month - 1]} ${two(year % 100)}"
    }

    private fun two(n: Int) = n.toString().padStart(2, '0')

    /** Days since 1970-01-01 to (year, month, day); Howard Hinnant's civil_from_days. */
    private fun civil(days: Long): Triple<Int, Int, Int> {
        val z = days + 719_468
        val era = (if (z >= 0) z else z - 146_096) / 146_097
        val doe = z - era * 146_097
        val yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = if (mp < 10) mp + 3 else mp - 9
        val y = yoe + era * 400 + if (m <= 2) 1 else 0
        return Triple(y.toInt(), m.toInt(), d.toInt())
    }
}
