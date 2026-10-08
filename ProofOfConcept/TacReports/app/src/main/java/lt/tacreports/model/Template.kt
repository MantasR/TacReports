package lt.tacreports.model

import java.util.UUID

enum class FieldKind {
    /** Asked when filling in the report. */
    INPUT,
    /** Printed as is, never asked (e.g. "Raportas: SITREP"). */
    FIXED,
    /**
     * A section title such as "B Savos pajėgos"; dropped when its whole section is empty.
     * The section runs to the next header; a header with no label just ends it.
     */
    HEADER,
}

/**
 * One line of a report template.
 *
 * [value] is the fixed text for [FieldKind.FIXED] and the grey hint for [FieldKind.INPUT].
 * [choices] are tap-to-pick options, one per entry, written "CODE = description" or just "CODE".
 * [remember] pre-fills the field with the last value typed into any field with the same label,
 * which suits per-mission values like "Kam" and "Nuo".
 */
data class Field(
    val id: String = newId(),
    val label: String = "",
    val kind: FieldKind = FieldKind.INPUT,
    val value: String = "",
    val remember: Boolean = false,
    val choices: List<String> = emptyList(),
)

data class Template(
    val id: String = newId(),
    val name: String = "",
    val fields: List<Field> = emptyList(),
)

/** A parsed entry of [Field.choices]: [code] goes into the report, [text] is shown on the chip. */
data class Choice(val code: String, val text: String) {
    companion object {
        fun parse(raw: String): Choice? {
            val s = raw.trim()
            if (s.isEmpty()) return null
            val eq = s.indexOf('=')
            if (eq < 0) return Choice(s, s)
            val code = s.substring(0, eq).trim()
            val desc = s.substring(eq + 1).trim()
            return if (code.isEmpty()) Choice(desc, desc) else Choice(code, if (desc.isEmpty()) code else "$code – $desc")
        }
    }
}

/** Key under which a remembered value is stored: the label, case- and space-insensitive. */
fun rememberKey(label: String): String = label.trim().lowercase().replace(Regex("\\s+"), " ")

/** Adds [code] to the space-separated [value], or removes it when it is already there. */
fun toggleChoice(value: String, code: String): String {
    val tokens = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return if (code in tokens) tokens.filter { it != code }.joinToString(" ")
    else (tokens + code).joinToString(" ")
}

fun newId(): String = UUID.randomUUID().toString().substring(0, 8)
