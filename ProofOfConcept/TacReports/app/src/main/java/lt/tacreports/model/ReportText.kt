package lt.tacreports.model

/** Turns a filled-in template into the text that goes to the clipboard: one point per line. */
object ReportText {

    fun render(template: Template, values: Map<String, String>): String {
        val out = ArrayList<String>()
        var pendingHeader: String? = null
        for (f in template.fields) {
            when (f.kind) {
                FieldKind.HEADER -> pendingHeader = f.label.trim().takeIf { it.isNotEmpty() }
                FieldKind.FIXED -> line(f.label, f.value)?.let { emit(out, pendingHeader, it); pendingHeader = null }
                FieldKind.INPUT -> {
                    val v = values[f.id].orEmpty().trim()
                    if (v.isNotEmpty()) {
                        emit(out, pendingHeader, line(f.label, v)!!)
                        pendingHeader = null
                    }
                }
            }
        }
        return out.joinToString("\n")
    }

    private fun emit(out: MutableList<String>, header: String?, line: String) {
        if (header != null) out += header
        out += line
    }

    private fun line(label: String, value: String): String? {
        val l = label.trim()
        val v = value.trim()
        return when {
            l.isEmpty() && v.isEmpty() -> null
            l.isEmpty() -> v
            v.isEmpty() -> l
            else -> "$l: $v"
        }
    }
}
