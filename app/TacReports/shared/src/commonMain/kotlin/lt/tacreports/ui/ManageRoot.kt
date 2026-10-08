package lt.tacreports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import lt.tacreports.Strings
import lt.tacreports.model.Template
import lt.tacreports.model.TemplateJson
import lt.tacreports.model.TemplateStore
import lt.tacreports.rememberPlatformActions
import lt.tacreports.strings

/** The app's main screen: template list and settings, or the editor of one template. */
@Composable
fun ManageRoot(version: String, onFill: (String) -> Unit, quickPanel: @Composable () -> Unit) {
    val s = strings()
    val platform = rememberPlatformActions()
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    fun share(list: List<Template>) = platform.share(TemplateJson.toJson(list), s.share)

    Box(Modifier.fillMaxSize().background(Tac.Bg)) {
        val template = editing?.let { TemplateStore.get(it) }
        if (template != null) {
            EditorScreen(
                template,
                onBack = { editing = null },
                onShare = { share(listOf(it)) },
                onDuplicate = { t -> TemplateStore.duplicate(t.id, s.copySuffix)?.let { editing = it.id } },
            )
        } else {
            HomeScreen(
                version = version,
                quickPanel = quickPanel,
                actions = HomeActions(
                    onFill = onFill,
                    onEdit = { editing = it },
                    onNew = {
                        val t = Template(name = s.newTemplateName)
                        TemplateStore.put(t)
                        editing = t.id
                    },
                    onImport = { platform.notify(importTemplates(platform.paste(), s)) },
                    onShareAll = { share(TemplateStore.templates.value) },
                ),
            )
        }
    }
}

/** Imports a shared template set from [text]; returns the message to show. */
fun importTemplates(text: String, s: Strings): String {
    val list = TemplateJson.fromJson(text)
    return if (list.isNullOrEmpty()) s.importNone else s.imported(TemplateStore.import(list))
}
