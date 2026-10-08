package lt.tacreports.model

import org.json.JSONArray
import org.json.JSONObject

/** JSON form of templates, used for storage and for sharing a set with the team. */
object TemplateJson {
    private const val FORMAT = "tacreports"

    fun toJson(templates: List<Template>): String = JSONObject()
        .put("format", FORMAT)
        .put("version", 1)
        .put("templates", JSONArray().apply { templates.forEach { put(template(it)) } })
        .toString(1)

    /** Reads what [toJson] wrote; also accepts a bare template object or array. Null when it isn't ours. */
    fun fromJson(text: String): List<Template>? = runCatching {
        val s = text.trim()
        val start = s.indexOfFirst { it == '{' || it == '[' }
        if (start < 0) return null
        val body = s.substring(start)
        val arr = if (body.startsWith("[")) JSONArray(body) else {
            val o = JSONObject(body)
            when {
                o.has("templates") -> o.getJSONArray("templates")
                o.has("fields") -> JSONArray().put(o)
                else -> return null
            }
        }
        (0 until arr.length()).map { template(arr.getJSONObject(it)) }
    }.getOrNull()

    private fun template(t: Template) = JSONObject()
        .put("id", t.id)
        .put("name", t.name)
        .put("fields", JSONArray().apply { t.fields.forEach { put(field(it)) } })

    private fun field(f: Field) = JSONObject().apply {
        put("id", f.id)
        put("label", f.label)
        put("kind", f.kind.name)
        if (f.value.isNotEmpty()) put("value", f.value)
        if (f.remember) put("remember", true)
        if (f.choices.isNotEmpty()) put("choices", JSONArray(f.choices))
    }

    private fun template(o: JSONObject): Template {
        val fa = o.optJSONArray("fields") ?: JSONArray()
        return Template(
            id = o.optString("id").ifEmpty { newId() },
            name = o.optString("name"),
            fields = (0 until fa.length()).map { field(fa.getJSONObject(it)) },
        )
    }

    private fun field(o: JSONObject): Field {
        val ca = o.optJSONArray("choices")
        return Field(
            id = o.optString("id").ifEmpty { newId() },
            label = o.optString("label"),
            kind = runCatching { FieldKind.valueOf(o.optString("kind")) }.getOrDefault(FieldKind.INPUT),
            value = o.optString("value"),
            remember = o.optBoolean("remember"),
            choices = if (ca == null) emptyList() else (0 until ca.length()).map { ca.getString(it) },
        )
    }
}
