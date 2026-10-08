package lt.tacreports.geo

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * WGS84 latitude/longitude to MGRS, e.g. "35U LA 89106 61342".
 * UTM uses the classic series (sub-millimetre inside a zone), with the Norway and Svalbard zone
 * exceptions. Digits are truncated, not rounded, as MGRS requires. Returns null outside 80°S–84°N.
 */
object Mgrs {
    private const val A = 6378137.0
    private const val F = 1 / 298.257223563
    private const val K0 = 0.9996
    private const val E2 = F * (2 - F)
    private const val EP2 = E2 / (1 - E2)
    private const val BANDS = "CDEFGHJKLMNPQRSTUVWX"
    private val COLUMNS = arrayOf("ABCDEFGH", "JKLMNPQR", "STUVWXYZ")
    private const val ROWS = "ABCDEFGHJKLMNPQRSTUV"

    data class Utm(val zone: Int, val band: Char, val easting: Double, val northing: Double)

    fun utm(lat: Double, lon: Double): Utm? {
        if (lat < -80.0 || lat > 84.0 || lon.isNaN()) return null
        val lon0 = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0
        var zone = (floor((lon0 + 180.0) / 6.0).toInt() + 1).coerceIn(1, 60)
        if (lat >= 56.0 && lat < 64.0 && lon0 >= 3.0 && lon0 < 12.0) zone = 32
        if (lat >= 72.0) zone = when {
            lon0 >= 0.0 && lon0 < 9.0 -> 31
            lon0 >= 9.0 && lon0 < 21.0 -> 33
            lon0 >= 21.0 && lon0 < 33.0 -> 35
            lon0 >= 33.0 && lon0 < 42.0 -> 37
            else -> zone
        }
        val band = BANDS[(floor((lat + 80.0) / 8.0).toInt()).coerceIn(0, BANDS.length - 1)]

        val phi = lat * (PI / 180.0)
        val dLam = (lon0 - (zone * 6 - 183)) * (PI / 180.0)
        val sinP = sin(phi)
        val cosP = cos(phi)
        val tanP = tan(phi)
        val n = A / sqrt(1 - E2 * sinP * sinP)
        val t = tanP * tanP
        val c = EP2 * cosP * cosP
        val a = cosP * dLam
        val e4 = E2 * E2
        val e6 = e4 * E2
        val m = A * (
            (1 - E2 / 4 - 3 * e4 / 64 - 5 * e6 / 256) * phi -
                (3 * E2 / 8 + 3 * e4 / 32 + 45 * e6 / 1024) * sin(2 * phi) +
                (15 * e4 / 256 + 45 * e6 / 1024) * sin(4 * phi) -
                (35 * e6 / 3072) * sin(6 * phi)
            )
        val easting = K0 * n * (
            a + (1 - t + c) * a * a * a / 6 +
                (5 - 18 * t + t * t + 72 * c - 58 * EP2) * a * a * a * a * a / 120
            ) + 500000.0
        var northing = K0 * (
            m + n * tanP * (
                a * a / 2 + (5 - t + 9 * c + 4 * c * c) * a * a * a * a / 24 +
                    (61 - 58 * t + t * t + 600 * c - 330 * EP2) * a * a * a * a * a * a / 720
                )
            )
        if (lat < 0) northing += 10000000.0
        return Utm(zone, band, easting, northing)
    }

    /** [digits] per axis: 5 = 1 m, 4 = 10 m, 3 = 100 m. */
    fun format(lat: Double, lon: Double, digits: Int = 5): String? {
        val u = utm(lat, lon) ?: return null
        val e100k = floor(u.easting / 100000.0).toInt()
        val n100k = floor(u.northing / 100000.0).toInt()
        val col = COLUMNS[(u.zone - 1) % 3][(e100k - 1).coerceIn(0, 7)]
        val row = ROWS[((n100k + if (u.zone % 2 == 0) 5 else 0) % 20 + 20) % 20]
        val d = digits.coerceIn(1, 5)
        val div = 10.0.pow(5 - d)
        val e = floor((u.easting - e100k * 100000.0) / div).toLong()
        val nn = floor((u.northing - n100k * 100000.0) / div).toLong()
        return "${u.zone}${u.band} $col$row ${e.toString().padStart(d, '0')} ${nn.toString().padStart(d, '0')}"
    }
}
