package lt.tacreports.geo

import androidx.compose.runtime.Composable

/** Outcome of one position request for the MGRS button. */
sealed interface Fix {
    data class At(val lat: Double, val lon: Double, val ageMs: Long) : Fix
    data object NoPermission : Fix
    data object None : Fix
}

/** Gets one position, asking for the location permission first when needed. */
fun interface Locator {
    suspend fun fix(): Fix
}

@Composable
expect fun rememberLocator(): Locator
