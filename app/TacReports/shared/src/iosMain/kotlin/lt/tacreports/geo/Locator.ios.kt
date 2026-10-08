package lt.tacreports.geo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSinceNow
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
actual fun rememberLocator(): Locator = remember { IosLocator() }

/**
 * One position from Core Location, the iOS counterpart of Android's OneFix: a recent, accurate
 * cached fix is used at once; otherwise it waits up to 30 s for a fresh one and falls back to the cache.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosLocator : Locator {
    private val delegate = Delegate()
    private val manager = CLLocationManager().also {
        it.delegate = delegate
        it.desiredAccuracy = kCLLocationAccuracyBest
    }

    private fun authorized() = manager.authorizationStatus.let {
        it == kCLAuthorizationStatusAuthorizedWhenInUse || it == kCLAuthorizationStatusAuthorizedAlways
    }

    override suspend fun fix(): Fix {
        if (manager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
            val answered = CompletableDeferred<Unit>()
            delegate.onAuthorization = { if (manager.authorizationStatus != kCLAuthorizationStatusNotDetermined) answered.complete(Unit) }
            manager.requestWhenInUseAuthorization()
            withTimeoutOrNull(120_000) { answered.await() }
            delegate.onAuthorization = null
        }
        if (!authorized()) return Fix.NoPermission

        manager.location?.let { if (ageMs(it) < 60_000 && it.horizontalAccuracy in 0.0..30.0) return at(it) }
        val fresh = withTimeoutOrNull(30_000) {
            suspendCancellableCoroutine<CLLocation?> { cont ->
                delegate.onLocation = { loc -> if (cont.isActive) cont.resume(loc) }
                cont.invokeOnCancellation { manager.stopUpdatingLocation() }
                manager.requestLocation()
            }
        }
        delegate.onLocation = null
        val loc = fresh ?: manager.location ?: return Fix.None
        return at(loc)
    }

    private fun ageMs(loc: CLLocation) = (-loc.timestamp.timeIntervalSinceNow * 1000).toLong()

    private fun at(loc: CLLocation): Fix = loc.coordinate.useContents { Fix.At(latitude, longitude, ageMs(loc)) }
}

private class Delegate : NSObject(), CLLocationManagerDelegateProtocol {
    var onAuthorization: (() -> Unit)? = null
    var onLocation: ((CLLocation?) -> Unit)? = null

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorization?.invoke()
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        onLocation?.invoke(didUpdateLocations.lastOrNull() as? CLLocation)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onLocation?.invoke(null)
    }
}
