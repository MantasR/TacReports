package lt.tacreports.geo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred

private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

private fun Context.hasLocation() = PERMISSIONS.any { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

/** Asks for the location permission through the screen's activity, then takes one [OneFix]. */
@Composable
actual fun rememberLocator(): Locator {
    val context = LocalContext.current
    val waiting = remember { arrayOfNulls<CompletableDeferred<Boolean>>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        waiting[0]?.complete(granted.values.any { it })
        waiting[0] = null
    }
    return remember(context, launcher) {
        Locator {
            if (!context.hasLocation()) {
                val answer = CompletableDeferred<Boolean>().also { waiting[0] = it }
                launcher.launch(PERMISSIONS)
                if (!answer.await()) return@Locator Fix.NoPermission
            }
            val loc = OneFix.get(context) ?: return@Locator Fix.None
            Fix.At(loc.latitude, loc.longitude, System.currentTimeMillis() - loc.time)
        }
    }
}
