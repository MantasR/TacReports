package lt.tacreports

import lt.tacreports.model.Dtg
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DtgTest {
    private val vilnius = ZoneId.of("Europe/Vilnius")

    @Test fun summerIsC() =
        assertEquals("03 1825 C OCT 26", Dtg.format(ZonedDateTime.of(2026, 10, 3, 18, 25, 0, 0, vilnius)))

    @Test fun winterIsB() =
        assertEquals("05 0704 B JAN 27", Dtg.format(ZonedDateTime.of(2027, 1, 5, 7, 4, 0, 0, vilnius)))

    @Test fun letters() {
        assertEquals('Z', Dtg.zoneLetter(0))
        assertEquals('A', Dtg.zoneLetter(3600))
        assertEquals('I', Dtg.zoneLetter(9 * 3600))
        assertEquals('K', Dtg.zoneLetter(10 * 3600))
        assertEquals('M', Dtg.zoneLetter(12 * 3600))
        assertEquals('N', Dtg.zoneLetter(-3600))
        assertEquals('Y', Dtg.zoneLetter(-12 * 3600))
    }

    @Test fun halfHourZoneFallsBackToUtc() =
        assertEquals("03 1255 Z OCT 26", Dtg.format(ZonedDateTime.of(2026, 10, 3, 18, 25, 0, 0, ZoneId.of("Asia/Kolkata"))))
}
