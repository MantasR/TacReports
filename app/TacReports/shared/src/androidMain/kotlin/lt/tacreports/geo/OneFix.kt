package lt.tacreports.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Gets one position from the platform LocationManager (no Play Services).
 * A recent, accurate last-known fix is used at once; otherwise it waits up to [timeoutMs]
 * for a fresh GPS fix and falls back to the newest last-known one.
 */
internal object OneFix {
    private const val FRESH_MS = 60_000L

    @SuppressLint("MissingPermission")
    suspend fun get(context: Context, timeoutMs: Long = 30_000L): Location? {
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val last = lm.allProviders
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (last != null && System.currentTimeMillis() - last.time < FRESH_MS && last.accuracy <= 30f) return last

        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return last
        }
        val fresh = withTimeoutOrNull(timeoutMs) { single(lm, provider, context) }
        return fresh ?: last
    }

    @SuppressLint("MissingPermission")
    private suspend fun single(lm: LocationManager, provider: String, context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            if (Build.VERSION.SDK_INT >= 30) {
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                lm.getCurrentLocation(provider, signal, context.mainExecutor) { loc -> if (cont.isActive) cont.resume(loc) }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lm.removeUpdates(this)
                        if (cont.isActive) cont.resume(location)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {}
                }
                cont.invokeOnCancellation { lm.removeUpdates(listener) }
                lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        }
}
