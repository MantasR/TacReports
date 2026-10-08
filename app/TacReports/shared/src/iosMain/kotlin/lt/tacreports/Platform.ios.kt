package lt.tacreports

import androidx.compose.runtime.Composable
import lt.tacreports.ui.Messages
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.preferredLanguages
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

/** NSUserDefaults-backed store; the template list is one JSON string in it. */
object IosKeyValueStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String?) {
        if (value == null) defaults.removeObjectForKey(key) else defaults.setObject(value, key)
    }
}

private object IosActions : PlatformActions {
    override fun copy(label: String, text: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    override fun paste(): String = UIPasteboard.generalPasteboard.string.orEmpty()

    override fun share(text: String, title: String) {
        val top = topViewController() ?: return
        val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        // iPad shows the sheet as a popover, which needs an anchor.
        sheet.popoverPresentationController?.sourceView = top.view
        top.presentViewController(sheet, animated = true, completion = null)
    }

    override fun notify(text: String) = Messages.show(text)

    override val ownCopyNotice: Boolean get() = false

    @Suppress("DEPRECATION")
    private fun topViewController(): UIViewController? {
        var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (vc?.presentedViewController != null) vc = vc.presentedViewController
        return vc
    }
}

@Composable
actual fun rememberPlatformActions(): PlatformActions = IosActions

@Composable
actual fun PlatformBackHandler(onBack: () -> Unit) {
}

actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun utcOffsetSeconds(atMillis: Long): Int =
    NSTimeZone.localTimeZone.secondsFromGMTForDate(NSDate.dateWithTimeIntervalSince1970(atMillis / 1000.0)).toInt()

actual fun systemLanguage(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.substringBefore('-').orEmpty()
