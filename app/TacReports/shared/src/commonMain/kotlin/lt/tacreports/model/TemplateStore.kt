package lt.tacreports.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lt.tacreports.KeyValueStore

/**
 * The template list (JSON in the platform's key-value store, seeded with [Defaults]), the last values
 * of "remember" fields, and unfinished drafts (in memory only, cleared when a report is copied).
 */
object TemplateStore {
    private const val KEY = "templates"
    private lateinit var kv: KeyValueStore
    private val state = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = state

    /** Draft values per template id, kept while the app process lives. */
    val drafts = HashMap<String, Map<String, String>>()

    fun init(store: KeyValueStore) {
        if (::kv.isInitialized) return
        kv = store
        state.value = kv.getString(KEY)?.let(TemplateJson::fromJson) ?: Defaults.all()
    }

    fun get(id: String): Template? = state.value.firstOrNull { it.id == id }

    fun put(t: Template) {
        val list = state.value
        val i = list.indexOfFirst { it.id == t.id }
        save(if (i < 0) list + t else list.toMutableList().also { it[i] = t })
    }

    fun delete(id: String) {
        drafts.remove(id)
        save(state.value.filterNot { it.id == id })
    }

    fun move(id: String, by: Int) {
        val list = state.value.toMutableList()
        val i = list.indexOfFirst { it.id == id }
        val j = i + by
        if (i < 0 || j !in list.indices) return
        list.add(j, list.removeAt(i))
        save(list)
    }

    fun duplicate(id: String, suffix: String): Template? {
        val t = get(id) ?: return null
        val copy = t.copy(id = newId(), name = "${t.name} $suffix".trim(), fields = t.fields.map { it.copy(id = newId()) })
        val list = state.value.toMutableList()
        list.add(list.indexOfFirst { it.id == id } + 1, copy)
        save(list)
        return copy
    }

    /** Adds shared templates; ones whose id already exists replace the old version. Returns how many. */
    fun import(incoming: List<Template>): Int {
        var list = state.value
        for (t in incoming) {
            val i = list.indexOfFirst { it.id == t.id }
            list = if (i < 0) list + t else list.toMutableList().also { it[i] = t }
        }
        save(list)
        return incoming.size
    }

    /** Puts back any of the built-in examples that were deleted. */
    fun restoreExamples() {
        val have = state.value.map { it.id }.toSet()
        save(state.value + Defaults.all().filter { it.id !in have })
    }

    private fun save(list: List<Template>) {
        state.value = list
        kv.putString(KEY, TemplateJson.toJson(list))
    }

    fun rememberedValue(label: String): String = kv.getString("remember:" + rememberKey(label)).orEmpty()

    fun remember(label: String, value: String) = kv.putString("remember:" + rememberKey(label), value.trim())
}
