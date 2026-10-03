package lt.tacreports

import lt.tacreports.geo.Mgrs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Expected values from the Python `mgrs` package (GeoTrans). */
class MgrsTest {
    private fun check(lat: Double, lon: Double, expected: String) =
        assertEquals(expected, Mgrs.format(lat, lon)!!.replace(" ", ""))

    @Test fun vilnius() = check(54.6872, 25.2797, "35ULA8910661342")
    @Test fun klaipeda() = check(55.7033, 21.1443, "34UEG0906873067")
    @Test fun zoneEdge24E() = check(54.0, 24.0, "35ULV0337987687")
    @Test fun justWestOf24E() = check(54.0, 23.9999, "34UFE9661487687")
    @Test fun bandV() = check(56.45, 26.8, "35VMC8767156183")
    @Test fun southernAlytus() = check(53.9, 23.5, "34UFE6425075291")
    @Test fun southernHemisphere() = check(-33.86, 151.21, "56HLH3441651925")
    @Test fun westernHemisphere() = check(40.0, -74.0, "18TWK8536028236")
    @Test fun equator() = check(0.0, 0.0, "31NAA6602100000")
    @Test fun north() = check(69.6, 18.9, "34WDC1831922670")

    @Test fun spacingAndPrecision() {
        assertEquals("35U LA 89106 61342", Mgrs.format(54.6872, 25.2797))
        assertEquals("35U LA 8910 6134", Mgrs.format(54.6872, 25.2797, 4))
        assertEquals("35U LA 891 613", Mgrs.format(54.6872, 25.2797, 3))
    }

    @Test fun outOfRange() = assertNull(Mgrs.format(85.0, 10.0))
}
