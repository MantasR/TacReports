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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import lt.tacreports.Prefs
import lt.tacreports.model.FieldKind
import lt.tacreports.model.TemplateStore
import lt.tacreports.strings

/** Public policy page; Google Play requires a link to it inside the app. */
const val PRIVACY_URL = "https://github.com/MantasR/TacReports/blob/main/PRIVACY.md"

/** Callbacks the home screen needs from its host. */
class HomeActions(
    val onFill: (String) -> Unit,
    val onEdit: (String) -> Unit,
    val onNew: () -> Unit,
    val onImport: () -> Unit,
    val onShareAll: () -> Unit,
)

/** Template list and settings. [quickPanel] is the platform's way in: the bubble on Android. */
@Composable
fun HomeScreen(version: String, actions: HomeActions, quickPanel: @Composable () -> Unit) {
    val s = strings()
    val templates by TemplateStore.templates.collectAsState()
    val language by Prefs.language.collectAsState()
    val mgrsDigits by Prefs.mgrsDigits.collectAsState()
    val uriHandler = LocalUriHandler.current
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

        quickPanel()

        TacPanel(Modifier.fillMaxWidth()) {
            SectionLabel(s.templates)
            for ((i, t) in templates.withIndex()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(Modifier.weight(1f).clickable { actions.onEdit(t.id) }.padding(vertical = 6.dp)) {
                        Text(t.name.ifBlank { "—" }, style = TacType.Body)
                        Text(s.fieldCount(t.fields.count { it.kind == FieldKind.INPUT }), style = TacType.Small)
                    }
                    TacButton("▲", { TemplateStore.move(t.id, -1) }, small = true, enabled = i > 0)
                    TacButton("▼", { TemplateStore.move(t.id, 1) }, small = true, enabled = i < templates.size - 1)
                    TacButton(s.fill, { actions.onFill(t.id) }, small = true, amber = true)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacButton(s.newTemplate, actions.onNew, Modifier.weight(1f), small = true)
                TacButton(s.importClipboard, actions.onImport, Modifier.weight(1f), small = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacButton(s.shareAll, actions.onShareAll, Modifier.weight(1f), small = true)
                TacButton(s.restoreExamples, { TemplateStore.restoreExamples() }, Modifier.weight(1f), small = true)
            }
        }

        TacPanel(Modifier.fillMaxWidth()) {
            SectionLabel(s.language)
            ChipRow {
                TacChip(s.languageAuto, language == null) { Prefs.setLanguage(null) }
                TacChip("Lietuvių", language == "lt") { Prefs.setLanguage("lt") }
                TacChip("English", language == "en") { Prefs.setLanguage("en") }
            }
            SectionLabel(s.mgrsPrecision)
            ChipRow {
                TacChip("10 (1 m)", mgrsDigits == 5) { Prefs.setMgrsDigits(5) }
                TacChip("8 (10 m)", mgrsDigits == 4) { Prefs.setMgrsDigits(4) }
                TacChip("6 (100 m)", mgrsDigits == 3) { Prefs.setMgrsDigits(3) }
            }
            Text(s.dtgHelp, style = TacType.Small)
            TacButton(s.privacyPolicy, { uriHandler.openUri(PRIVACY_URL) }, Modifier.fillMaxWidth(), small = true)
        }
    }
}
