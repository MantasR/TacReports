package lt.tacreports

import lt.tacreports.model.Dtg
import kotlin.test.Test
import kotlin.test.assertEquals

class DtgTest {
    private val summer = 3 * 3600
    private val winter = 2 * 3600

    // 2026-10-03 15:25 UTC = 18:25 in Vilnius (summer time).
    @Test fun summerIsC() = assertEquals("03 1825 C OCT 26", Dtg.format(1_791_041_100_000L, summer))

    // 2027-01-05 05:04 UTC = 07:04 in Vilnius (winter time).
    @Test fun winterIsB() = assertEquals("05 0704 B JAN 27", Dtg.format(1_799_125_440_000L, winter))

    @Test fun letters() {
        assertEquals('Z', Dtg.zoneLetter(0))
        assertEquals('A', Dtg.zoneLetter(3600))
        assertEquals('I', Dtg.zoneLetter(9 * 3600))
        assertEquals('K', Dtg.zoneLetter(10 * 3600))
        assertEquals('M', Dtg.zoneLetter(12 * 3600))
        assertEquals('N', Dtg.zoneLetter(-3600))
        assertEquals('Y', Dtg.zoneLetter(-12 * 3600))
    }

    // 2026-10-03 12:55 UTC in India (UTC+5:30) falls back to UTC.
    @Test fun halfHourZoneFallsBackToUtc() =
        assertEquals("03 1255 Z OCT 26", Dtg.format(1_791_032_100_000L, 5 * 3600 + 1800))

    @Test fun leapDayAndYearEnd() {
        assertEquals("29 0000 Z FEB 28", Dtg.format(1_835_395_200_000L, 0))
        assertEquals("31 2359 Z DEC 26", Dtg.format(1_798_761_540_000L, 0))
    }
}
