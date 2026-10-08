package lt.tacreports

import androidx.compose.runtime.Composable
import lt.tacreports.model.TemplateStore

/** Small persisted strings: SharedPreferences on Android, NSUserDefaults on iOS. */
interface KeyValueStore {
    fun getString(key: String): String?

    /** Null removes the key. */
    fun putString(key: String, value: String?)
}

/** Loads the stored templates and settings; safe to call more than once. */
fun initStores(store: KeyValueStore) {
    TemplateStore.init(store)
    Prefs.init(store)
}

/** What the shared screens ask of the phone. */
interface PlatformActions {
    fun copy(label: String, text: String)
    fun paste(): String
    fun share(text: String, title: String)

    /** A short message (Android toast, iOS banner). */
    fun notify(text: String)

    /** True when the system itself confirms a clipboard copy (Android 13+). */
    val ownCopyNotice: Boolean
}

@Composable
expect fun rememberPlatformActions(): PlatformActions

/** System back (Android); iOS has none, the screens' own back buttons are used there. */
@Composable
expect fun PlatformBackHandler(onBack: () -> Unit)

expect fun nowMillis(): Long

/** The phone's UTC offset at [atMillis], daylight saving included. */
expect fun utcOffsetSeconds(atMillis: Long): Int

/** Two-letter code of the phone's language, e.g. "lt". */
expect fun systemLanguage(): String
