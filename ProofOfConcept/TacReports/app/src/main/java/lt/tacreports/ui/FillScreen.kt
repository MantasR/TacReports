package lt.tacreports.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import lt.tacreports.Prefs
import lt.tacreports.R
import lt.tacreports.geo.Mgrs
import lt.tacreports.geo.OneFix
import lt.tacreports.model.Choice
import lt.tacreports.model.Dtg
import lt.tacreports.model.FieldKind
import lt.tacreports.model.ReportText
import lt.tacreports.model.Template
import lt.tacreports.model.TemplateStore
import lt.tacreports.model.toggleChoice

/**
 * What the bubble opens: pick a template, fill in its blanks, copy. [onDone] closes the screen
 * so the user lands back in the messenger with the report on the clipboard.
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
            BackHandler(onBack = onDone)
            Picker(templates, onPick = { selected = it }, onManage = onManage, onClose = onDone)
        } else {
            BackHandler { if (initialId != null) onDone() else selected = null }
            FillForm(
                template,
                onBack = { if (initialId != null) onDone() else selected = null },
                onDone = onDone,
            )
        }
    }
}

@Composable
private fun Picker(templates: List<Template>, onPick: (String) -> Unit, onManage: () -> Unit, onClose: () -> Unit) {
    TacPanel(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = null, indication = null) {},
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.pick_title), style = TacType.Title, modifier = Modifier.weight(1f))
            TacButton("✕", onClose, small = true)
        }
        Column(
            Modifier.verticalScroll(rememberScrollState()).weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (templates.isEmpty()) Text(stringResource(R.string.no_templates), style = TacType.Body, color = Tac.TextDim)
            for (t in templates) {
                val draft = TemplateStore.drafts[t.id]?.values?.any { it.isNotBlank() } == true
                TacButton(
                    if (draft) "${t.name}  •  ${stringResource(R.string.draft)}" else t.name,
                    onClick = { onPick(t.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        TacButton(stringResource(R.string.manage_templates), onManage, modifier = Modifier.fillMaxWidth(), small = true)
    }
}

@Composable
private fun FillForm(template: Template, onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }
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
    var waitingPermissionFor by remember { mutableStateOf<String?>(null) }
    fun fetchMgrs(id: String) {
        gpsFor = id
        scope.launch {
            val loc = OneFix.get(context)
            gpsFor = null
            val mgrs = loc?.let { Mgrs.format(it.latitude, it.longitude, prefs.mgrsDigits) }
            if (loc == null || mgrs == null) {
                toast(context, context.getString(R.string.no_fix))
                return@launch
            }
            val ageMin = ((System.currentTimeMillis() - loc.time) / 60000).toInt()
            if (ageMin >= 2) toast(context, context.getString(R.string.old_fix, ageMin))
            insert(id, mgrs)
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        val id = waitingPermissionFor
        waitingPermissionFor = null
        if (id != null && granted.values.any { it }) fetchMgrs(id) else toast(context, context.getString(R.string.no_location_permission))
    }
    fun onMgrs(id: String) {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (ok) fetchMgrs(id) else {
            waitingPermissionFor = id
            permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    var preview by remember { mutableStateOf(false) }
    val text = ReportText.render(template, values.mapValues { it.value.text })

    fun copy() {
        val cm = context.getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(ClipData.newPlainText(template.name, text))
        for (f in template.fields) if (f.kind == FieldKind.INPUT && f.remember) TemplateStore.remember(f.label, values[f.id]?.text.orEmpty())
        TemplateStore.drafts.remove(template.id)
        // Android 13+ shows its own "Copied" confirmation.
        if (Build.VERSION.SDK_INT < 33) toast(context, context.getString(R.string.copied))
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
            TacButton(stringResource(R.string.clear), small = true, onClick = {
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
            TacButton(
                stringResource(if (preview) R.string.edit else R.string.preview),
                { preview = !preview },
                filled = preview,
            )
            TacButton(stringResource(R.string.copy_report), { copy() }, modifier = Modifier.weight(1f), amber = true, filled = true, enabled = text.isNotEmpty())
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

private fun toast(context: Context, text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
