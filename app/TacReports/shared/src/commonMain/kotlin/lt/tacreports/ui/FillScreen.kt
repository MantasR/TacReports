package lt.tacreports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import lt.tacreports.PlatformBackHandler
import lt.tacreports.Prefs
import lt.tacreports.geo.Fix
import lt.tacreports.geo.Mgrs
import lt.tacreports.geo.rememberLocator
import lt.tacreports.model.Choice
import lt.tacreports.model.Dtg
import lt.tacreports.model.FieldKind
import lt.tacreports.model.ReportText
import lt.tacreports.model.Template
import lt.tacreports.model.TemplateStore
import lt.tacreports.model.toggleChoice
import lt.tacreports.rememberPlatformActions
import lt.tacreports.strings

/**
 * Pick a template, fill in its blanks, copy. On Android the bubble opens this over the messenger
 * and [onDone] closes it, so the user lands back there with the report on the clipboard.
 */
@Composable
fun FillRoot(initialId: String?, onManage: () -> Unit, onDone: () -> Unit) {
    val templates by TemplateStore.templates.collectAsState()
    var selected by rememberSaveable { mutableStateOf(initialId) }
    val template = selected?.let { id -> templates.firstOrNull { it.id == id } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Tac.Scrim)
            .clickable(interactionSource = null, indication = null) { if (template == null) onDone() }
            .safeDrawingPadding()
            .padding(10.dp),
    ) {
        if (template == null) {
            PlatformBackHandler(onDone)
            Picker(templates, onPick = { selected = it }, onManage = onManage, onClose = onDone)
        } else {
            val back: () -> Unit = { if (initialId != null) onDone() else selected = null }
            PlatformBackHandler(back)
            FillForm(template, onBack = back, onDone = onDone)
        }
    }
}

@Composable
private fun Picker(templates: List<Template>, onPick: (String) -> Unit, onManage: () -> Unit, onClose: () -> Unit) {
    val s = strings()
    TacPanel(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = null, indication = null) {},
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.pickTitle, style = TacType.Title, modifier = Modifier.weight(1f))
            TacButton("✕", onClose, small = true)
        }
        Column(
            Modifier.verticalScroll(rememberScrollState()).weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (templates.isEmpty()) Text(s.noTemplates, style = TacType.Body, color = Tac.TextDim)
            for (t in templates) {
                val draft = TemplateStore.drafts[t.id]?.values?.any { it.isNotBlank() } == true
                TacButton(
                    if (draft) "${t.name}  •  ${s.draft}" else t.name,
                    onClick = { onPick(t.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        TacButton(s.manageTemplates, onManage, modifier = Modifier.fillMaxWidth(), small = true)
    }
}

@Composable
private fun FillForm(template: Template, onBack: () -> Unit, onDone: () -> Unit) {
    val s = strings()
    val platform = rememberPlatformActions()
    val locator = rememberLocator()
    val scope = rememberCoroutineScope()
    val values = remember(template.id) {
        val draft = TemplateStore.drafts[template.id]
        mutableStateMapOf<String, TextFieldValue>().apply {
            for (f in template.fields) if (f.kind == FieldKind.INPUT) {
                val v = draft?.get(f.id) ?: if (f.remember) TemplateStore.rememberedValue(f.label) else ""
                put(f.id, TextFieldValue(v, TextRange(v.length)))
            }
        }
    }
    fun set(id: String, v: TextFieldValue) {
        values[id] = v
        TemplateStore.drafts[template.id] = values.mapValues { it.value.text }
    }
    fun insert(id: String, text: String) {
        val cur = values[id] ?: TextFieldValue("")
        val start = cur.selection.min
        val end = cur.selection.max
        val before = cur.text.substring(0, start)
        val pad = if (before.isNotEmpty() && !before.last().isWhitespace()) " " else ""
        val newText = before + pad + text + cur.text.substring(end)
        set(id, TextFieldValue(newText, TextRange(start + pad.length + text.length)))
    }

    var gpsFor by remember { mutableStateOf<String?>(null) }
    fun onMgrs(id: String) {
        gpsFor = id
        scope.launch {
            val fix = locator.fix()
            gpsFor = null
            val mgrs = (fix as? Fix.At)?.let { Mgrs.format(it.lat, it.lon, Prefs.mgrsDigits.value) }
            when {
                fix is Fix.NoPermission -> platform.notify(s.noLocationPermission)
                fix !is Fix.At || mgrs == null -> platform.notify(s.noFix)
                else -> {
                    val ageMin = (fix.ageMs / 60_000).toInt()
                    if (ageMin >= 2) platform.notify(s.oldFix(ageMin))
                    insert(id, mgrs)
                }
            }
        }
    }

    var preview by remember { mutableStateOf(false) }
    val text = ReportText.render(template, values.mapValues { it.value.text })

    fun copy() {
        platform.copy(template.name, text)
        for (f in template.fields) if (f.kind == FieldKind.INPUT && f.remember) TemplateStore.remember(f.label, values[f.id]?.text.orEmpty())
        TemplateStore.drafts.remove(template.id)
        if (!platform.ownCopyNotice) platform.notify(s.copied)
        onDone()
    }

    TacPanel(
        Modifier
            .fillMaxSize()
            .clickable(interactionSource = null, indication = null) {},
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TacButton("‹", onBack, small = true)
            Text(template.name, style = TacType.Title, modifier = Modifier.weight(1f))
            TacButton(s.clear, small = true, onClick = {
                for (f in template.fields) if (f.kind == FieldKind.INPUT) values[f.id] = TextFieldValue("")
                TemplateStore.drafts.remove(template.id)
            })
        }
        if (preview) {
            Column(Modifier.weight(1f).fillMaxWidth().background(Tac.Field).verticalScroll(rememberScrollState()).padding(10.dp)) {
                Text(text.ifEmpty { "—" }, style = TacType.Value)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(template.fields, key = { it.id }) { f ->
                    when (f.kind) {
                        FieldKind.HEADER ->
                            if (f.label.isBlank()) Spacer(Modifier.height(2.dp))
                            else SectionLabel(f.label, Modifier.padding(top = 6.dp), color = Tac.Cyan)
                        FieldKind.FIXED ->
                            Text(listOf(f.label, f.value).filter { it.isNotBlank() }.joinToString(": "), style = TacType.Small)
                        FieldKind.INPUT -> InputRow(
                            label = f.label,
                            hint = f.value,
                            choices = f.choices,
                            value = values[f.id] ?: TextFieldValue(""),
                            gpsBusy = gpsFor == f.id,
                            onChange = { set(f.id, it) },
                            onMgrs = { onMgrs(f.id) },
                            onDtg = { insert(f.id, Dtg.format()) },
                        )
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TacButton(if (preview) s.edit else s.preview, { preview = !preview }, filled = preview)
            TacButton(s.copyReport, { copy() }, modifier = Modifier.weight(1f), amber = true, filled = true, enabled = text.isNotEmpty())
        }
    }
}

@Composable
private fun InputRow(
    label: String,
    hint: String,
    choices: List<String>,
    value: TextFieldValue,
    gpsBusy: Boolean,
    onChange: (TextFieldValue) -> Unit,
    onMgrs: () -> Unit,
    onDtg: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = TacType.Body, modifier = Modifier.weight(1f))
            TacButton(if (gpsBusy) "GPS…" else "MGRS", onMgrs, small = true, amber = true, enabled = !gpsBusy)
            TacButton("DTG", onDtg, small = true, amber = true)
        }
        TacTextField(value, onChange, hint = hint)
        val parsed = choices.mapNotNull { Choice.parse(it) }
        if (parsed.isNotEmpty()) {
            val tokens = value.text.split(Regex("\\s+")).toSet()
            ChipRow {
                for (c in parsed) TacChip(c.text, c.code in tokens) {
                    val v = toggleChoice(value.text, c.code)
                    onChange(TextFieldValue(v, TextRange(v.length)))
                }
            }
        }
    }
}
