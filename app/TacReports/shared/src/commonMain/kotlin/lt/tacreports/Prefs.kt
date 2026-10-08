package lt.tacreports

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Persisted settings shared by both platforms; the screens observe them. */
object Prefs {
    private var kv: KeyValueStore? = null
    private val lang = MutableStateFlow<String?>(null)
    private val digits = MutableStateFlow(5)

    /** "lt", "en", or null to follow the phone's language. */
    val language: StateFlow<String?> = lang

    /** MGRS digits per axis: 5 (1 m), 4 (10 m) or 3 (100 m). */
    val mgrsDigits: StateFlow<Int> = digits

    fun init(store: KeyValueStore) {
        if (kv != null) return
        kv = store
        lang.value = store.getString("lang")
        digits.value = store.getString("mgrsDigits")?.toIntOrNull() ?: 5
    }

    fun setLanguage(v: String?) {
        lang.value = v
        kv?.putString("lang", v)
    }

    fun setMgrsDigits(v: Int) {
        digits.value = v
        kv?.putString("mgrsDigits", v.toString())
    }
}
