package lt.tacreports.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import lt.tacreports.R
import lt.tacreports.model.FieldKind
import lt.tacreports.model.TemplateStore

/** Callbacks the home screen needs from the activity. */
class HomeActions(
    val onBubble: (Boolean) -> Unit,
    val onFill: (String) -> Unit,
    val onEdit: (String) -> Unit,
    val onNew: () -> Unit,
    val onImport: () -> Unit,
    val onShareAll: () -> Unit,
    val onLanguage: (String?) -> Unit,
    val onMgrsDigits: (Int) -> Unit,
)

@Composable
fun HomeScreen(
    bubbleOn: Boolean,
    overlayAllowed: Boolean,
    language: String?,
    mgrsDigits: Int,
    version: String,
    actions: HomeActions,
) {
    val templates by TemplateStore.templates.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("TACREPORTS", style = TacType.Title.copy(fontSize = TacType.Title.fontSize * 1.3f), modifier = Modifier.weight(1f))
            Text(version, style = TacType.Small)
        }

        TacPanel(Modifier.fillMaxWidth(), color = if (bubbleOn) Tac.Amber else Tac.Cyan) {
            TacToggleRow(stringResource(R.string.bubble), bubbleOn, actions.onBubble)
            Text(stringResource(R.string.bubble_help), style = TacType.Small)
            if (bubbleOn && !overlayAllowed) {
                Text(stringResource(R.string.overlay_needed), style = TacType.Small, color = Tac.Amber)
            }
        }

        TacPanel(Modifier.fillMaxWidth()) {
            SectionLabel(stringResource(R.string.templates))
            for ((i, t) in templates.withIndex()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(Modifier.weight(1f).clickable { actions.onEdit(t.id) }.padding(vertical = 6.dp)) {
                        Text(t.name.ifBlank { "—" }, style = TacType.Body)
                        Text(
                            stringResource(R.string.field_count, t.fields.count { it.kind == FieldKind.INPUT }),
                            style = TacType.Small,
                        )
                    }
                    TacButton("▲", { TemplateStore.move(t.id, -1) }, small = true, enabled = i > 0)
                    TacButton("▼", { TemplateStore.move(t.id, 1) }, small = true, enabled = i < templates.size - 1)
                    TacButton(stringResource(R.string.fill), { actions.onFill(t.id) }, small = true, amber = true)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacButton(stringResource(R.string.new_template), actions.onNew, Modifier.weight(1f), small = true)
                TacButton(stringResource(R.string.import_clipboard), actions.onImport, Modifier.weight(1f), small = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacButton(stringResource(R.string.share_all), actions.onShareAll, Modifier.weight(1f), small = true)
                TacButton(stringResource(R.string.restore_examples), { TemplateStore.restoreExamples() }, Modifier.weight(1f), small = true)
            }
        }

        TacPanel(Modifier.fillMaxWidth()) {
            SectionLabel(stringResource(R.string.language))
            ChipRow {
                TacChip(stringResource(R.string.language_auto), language == null) { actions.onLanguage(null) }
                TacChip("Lietuvių", language == "lt") { actions.onLanguage("lt") }
                TacChip("English", language == "en") { actions.onLanguage("en") }
            }
            SectionLabel(stringResource(R.string.mgrs_precision))
            ChipRow {
                TacChip("10 (1 m)", mgrsDigits == 5) { actions.onMgrsDigits(5) }
                TacChip("8 (10 m)", mgrsDigits == 4) { actions.onMgrsDigits(4) }
                TacChip("6 (100 m)", mgrsDigits == 3) { actions.onMgrsDigits(3) }
            }
            Text(stringResource(R.string.dtg_help), style = TacType.Small)
        }
    }
}
