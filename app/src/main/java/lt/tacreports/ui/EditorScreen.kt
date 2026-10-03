package lt.tacreports.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import lt.tacreports.R
import lt.tacreports.model.Field
import lt.tacreports.model.FieldKind
import lt.tacreports.model.Template
import lt.tacreports.model.TemplateStore

/** Edits one template; changes are saved as you type. */
@Composable
fun EditorScreen(
    initial: Template,
    onBack: () -> Unit,
    onShare: (Template) -> Unit,
    onDuplicate: (Template) -> Unit,
) {
    var t by remember(initial.id) { mutableStateOf(initial) }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }
    val latest by rememberUpdatedState(t)

    LaunchedEffect(t) {
        delay(400)
        if (!deleted) TemplateStore.put(t)
    }
    DisposableEffect(initial.id) { onDispose { if (!deleted) TemplateStore.put(latest) } }
    BackHandler(onBack = onBack)

    fun update(i: Int, f: Field) { t = t.copy(fields = t.fields.toMutableList().also { it[i] = f }) }
    fun move(i: Int, by: Int) {
        val j = i + by
        if (j !in t.fields.indices) return
        t = t.copy(fields = t.fields.toMutableList().also { it.add(j, it.removeAt(i)) })
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TacButton("‹", onBack, small = true)
            Text(stringResource(R.string.edit_template), style = TacType.Title, modifier = Modifier.weight(1f))
            TacButton(stringResource(R.string.share), { onShare(t) }, small = true)
            TacButton(stringResource(R.string.duplicate), { TemplateStore.put(t); onDuplicate(t) }, small = true)
            TacButton(
                stringResource(if (confirmDelete) R.string.delete_confirm else R.string.delete),
                {
                    if (confirmDelete) {
                        deleted = true
                        TemplateStore.delete(t.id)
                        onBack()
                    } else confirmDelete = true
                },
                small = true, amber = true, filled = confirmDelete,
            )
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Column {
                    SectionLabel(stringResource(R.string.template_name))
                    Spacer(Modifier.height(4.dp))
                    TacTextField(t.name, { t = t.copy(name = it) }, singleLine = true, style = TacType.Body)
                }
            }
            itemsIndexed(t.fields, key = { _, f -> f.id }) { i, f ->
                FieldEditor(
                    f,
                    first = i == 0,
                    last = i == t.fields.size - 1,
                    onChange = { update(i, it) },
                    onUp = { move(i, -1) },
                    onDown = { move(i, 1) },
                    onRemove = { t = t.copy(fields = t.fields.filterIndexed { k, _ -> k != i }) },
                )
            }
            item { Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TacButton(stringResource(R.string.add_field), { t = t.copy(fields = t.fields + Field()) }, Modifier.weight(1f))
                    TacButton(
                        stringResource(R.string.add_section),
                        { t = t.copy(fields = t.fields + Field(kind = FieldKind.HEADER)) },
                        Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.editor_help), style = TacType.Small)
            } }
        }
    }
}

@Composable
private fun FieldEditor(
    f: Field,
    first: Boolean,
    last: Boolean,
    onChange: (Field) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onRemove: () -> Unit,
) {
    // Options are edited as raw text so a just-typed newline isn't trimmed away.
    var options by remember(f.id) { mutableStateOf(f.choices.joinToString("\n")) }
    val frame = when (f.kind) {
        FieldKind.INPUT -> Tac.Cyan
        FieldKind.FIXED -> Tac.CyanDim
        FieldKind.HEADER -> Tac.Amber
    }
    TacPanel(Modifier.fillMaxWidth(), color = frame) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TacChip(stringResource(R.string.kind_input), f.kind == FieldKind.INPUT) { onChange(f.copy(kind = FieldKind.INPUT)) }
                TacChip(stringResource(R.string.kind_fixed), f.kind == FieldKind.FIXED) { onChange(f.copy(kind = FieldKind.FIXED)) }
                TacChip(stringResource(R.string.kind_header), f.kind == FieldKind.HEADER) { onChange(f.copy(kind = FieldKind.HEADER)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(Modifier.weight(1f)) {
                TacTextField(
                    f.label, { onChange(f.copy(label = it)) },
                    hint = stringResource(if (f.kind == FieldKind.HEADER) R.string.hint_section else R.string.hint_label),
                    singleLine = true, style = TacType.Body,
                )
            }
            TacButton("▲", onUp, small = true, enabled = !first)
            TacButton("▼", onDown, small = true, enabled = !last)
            TacButton("✕", onRemove, small = true, amber = true)
        }
        when (f.kind) {
            FieldKind.FIXED -> TacTextField(f.value, { onChange(f.copy(value = it)) }, hint = stringResource(R.string.hint_fixed))
            FieldKind.INPUT -> {
                TacTextField(f.value, { onChange(f.copy(value = it)) }, hint = stringResource(R.string.hint_hint), singleLine = true)
                TacToggleRow(stringResource(R.string.remember), f.remember, { onChange(f.copy(remember = it)) })
                TacTextField(
                    options,
                    {
                        options = it
                        onChange(f.copy(choices = it.lines().map(String::trim).filter(String::isNotEmpty)))
                    },
                    hint = stringResource(R.string.hint_options),
                )
            }
            FieldKind.HEADER -> {}
        }
    }
}
