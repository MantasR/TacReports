package lt.tacreports

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

/** Small persisted settings. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("tacreports", Context.MODE_PRIVATE)

    /** "lt", "en", or null to follow the phone's language. */
    var language: String?
        get() = sp.getString("lang", null)
        set(v) = sp.edit { if (v == null) remove("lang") else putString("lang", v) }

    /** MGRS digits per axis: 5 (1 m), 4 (10 m) or 3 (100 m). */
    var mgrsDigits: Int
        get() = sp.getInt("mgrsDigits", 5)
        set(v) = sp.edit { putInt("mgrsDigits", v) }

    /** The user wants the floating bubble; it is restarted when the app opens. */
    var bubbleOn: Boolean
        get() = sp.getBoolean("bubble", false)
        set(v) = sp.edit { putBoolean("bubble", v) }

    var bubbleX: Int
        get() = sp.getInt("bubbleX", -1)
        set(v) = sp.edit { putInt("bubbleX", v) }

    var bubbleY: Int
        get() = sp.getInt("bubbleY", 300)
        set(v) = sp.edit { putInt("bubbleY", v) }

    companion object {
        /** Wraps a context so its resources use the chosen app language. */
        fun localized(base: Context): Context {
            val lang = Prefs(base).language ?: return base
            val config = Configuration(base.resources.configuration)
            config.setLocale(Locale.forLanguageTag(lang))
            return base.createConfigurationContext(config)
        }
    }
}
