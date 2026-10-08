package lt.tacreports.bubble

import android.content.Context
import androidx.core.content.edit

/** Whether the user wants the bubble, and where it was left. */
class BubblePrefs(context: Context) {
    private val sp = context.getSharedPreferences("bubble", Context.MODE_PRIVATE)

    /** The user wants the floating bubble; it is restarted when the app opens. */
    var on: Boolean
        get() = sp.getBoolean("on", false)
        set(v) = sp.edit { putBoolean("on", v) }

    var x: Int
        get() = sp.getInt("x", -1)
        set(v) = sp.edit { putInt("x", v) }

    var y: Int
        get() = sp.getInt("y", 300)
        set(v) = sp.edit { putInt("y", v) }
}
