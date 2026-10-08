package lt.tacreports

import android.content.Context
import lt.tacreports.bubble.BubblePrefs
import java.io.File

private var migrated = false

/** Loads the shared stores from SharedPreferences; every activity and service calls this first. */
fun initStores(context: Context) {
    val kv = AndroidKeyValueStore(context)
    if (!migrated) {
        migrateFrom010(context, kv)
        migrated = true
    }
    initStores(kv)
}

/**
 * 0.1.0 (the Android-only proof of concept, same app id) kept templates in templates.json,
 * remembered values in "remembered" and settings in "tacreports" (mgrsDigits as an Int). Copied
 * over once on the first start after an upgrade; the old files are left in place.
 */
private fun migrateFrom010(context: Context, kv: KeyValueStore) {
    if (kv.getString("migrated010") != null) return
    val templates = File(context.filesDir, "templates.json")
    if (templates.exists() && kv.getString("templates") == null) kv.putString("templates", templates.readText())
    for ((k, v) in context.getSharedPreferences("remembered", Context.MODE_PRIVATE).all) {
        if (v is String) kv.putString("remember:$k", v)
    }
    val old = context.getSharedPreferences("tacreports", Context.MODE_PRIVATE).all
    (old["lang"] as? String)?.let { kv.putString("lang", it) }
    (old["mgrsDigits"] as? Int)?.let { kv.putString("mgrsDigits", it.toString()) }
    val bubble = BubblePrefs(context)
    (old["bubble"] as? Boolean)?.let { bubble.on = it }
    (old["bubbleX"] as? Int)?.let { bubble.x = it }
    (old["bubbleY"] as? Int)?.let { bubble.y = it }
    kv.putString("migrated010", "1")
}
