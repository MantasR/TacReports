package lt.tacreports.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * JSON form of templates, used for storage and for sharing a set with the team.
 * Same layout as TacReports 0.1.0, so sets shared from that app import here too.
 */
object TemplateJson {
    private const val FORMAT = "tacreports"
    private val json = Json { prettyPrint = true }

    fun toJson(templates: List<Template>): String = json.encodeToString(
        JsonElement.serializer(),
        buildJsonObject {
            put("format", FORMAT)
            put("version", 1)
            putJsonArray("templates") { templates.forEach { add(template(it)) } }
        },
    )

    /** Reads what [toJson] wrote; also accepts a bare template object or array. Null when it isn't ours. */
    fun fromJson(text: String): List<Template>? = runCatching {
        val s = text.trim()
        val start = s.indexOfFirst { it == '{' || it == '[' }
        if (start < 0) return null
        // Text around the JSON (a messenger's caption, say) is ignored.
        val end = s.lastIndexOf(if (s[start] == '{') '}' else ']')
        if (end < start) return null
        val arr = when (val root = Json.parseToJsonElement(s.substring(start, end + 1))) {
            is JsonArray -> root
            is JsonObject -> when {
                "templates" in root -> root["templates"] as? JsonArray ?: return null
                "fields" in root -> JsonArray(listOf(root))
                else -> return null
            }
            else -> return null
        }
        arr.map { template(it as JsonObject) }
    }.getOrNull()

    private fun template(t: Template) = buildJsonObject {
        put("id", t.id)
        put("name", t.name)
        putJsonArray("fields") { t.fields.forEach { add(field(it)) } }
    }

    private fun field(f: Field) = buildJsonObject {
        put("id", f.id)
        put("label", f.label)
        put("kind", f.kind.name)
        if (f.value.isNotEmpty()) put("value", f.value)
        if (f.remember) put("remember", true)
        if (f.choices.isNotEmpty()) put("choices", buildJsonArray { f.choices.forEach { add(JsonPrimitive(it)) } })
    }

    private fun template(o: JsonObject): Template = Template(
        id = o.str("id").ifEmpty { newId() },
        name = o.str("name"),
        fields = (o["fields"] as? JsonArray).orEmpty().map { field(it as JsonObject) },
    )

    private fun field(o: JsonObject): Field = Field(
        id = o.str("id").ifEmpty { newId() },
        label = o.str("label"),
        kind = FieldKind.entries.firstOrNull { it.name == o.str("kind") } ?: FieldKind.INPUT,
        value = o.str("value"),
        remember = (o["remember"] as? JsonPrimitive)?.booleanOrNull ?: false,
        choices = (o["choices"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.content },
    )

    private fun JsonObject.str(key: String): String = (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content.orEmpty()
}
