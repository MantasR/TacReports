package lt.tacreports

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import java.util.TimeZone

/** SharedPreferences-backed store; the template list is one JSON string in it. */
class AndroidKeyValueStore(context: Context) : KeyValueStore {
    private val sp = context.applicationContext.getSharedPreferences("store", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = sp.getString(key, null)

    override fun putString(key: String, value: String?) {
        sp.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply()
    }
}

private class AndroidActions(private val context: Context) : PlatformActions {
    override fun copy(label: String, text: String) {
        context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(label, text))
    }

    override fun paste(): String {
        val clip = context.getSystemService(ClipboardManager::class.java).primaryClip
        return clip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
    }

    override fun share(text: String, title: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        val chooser = Intent.createChooser(send, title)
        if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override fun notify(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    // Android 13+ shows its own "Copied" confirmation.
    override val ownCopyNotice: Boolean get() = Build.VERSION.SDK_INT >= 33
}

@Composable
actual fun rememberPlatformActions(): PlatformActions {
    val context = LocalContext.current
    return remember(context) { AndroidActions(context) }
}

@Composable
actual fun PlatformBackHandler(onBack: () -> Unit) = BackHandler(onBack = onBack)

actual fun nowMillis(): Long = System.currentTimeMillis()

actual fun utcOffsetSeconds(atMillis: Long): Int = TimeZone.getDefault().getOffset(atMillis) / 1000

actual fun systemLanguage(): String = Locale.getDefault().language
