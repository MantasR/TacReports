package lt.tacreports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow
import lt.tacreports.ui.FillRoot
import lt.tacreports.ui.ManageRoot
import lt.tacreports.ui.MessageHost
import lt.tacreports.ui.SectionLabel
import lt.tacreports.ui.Tac
import lt.tacreports.ui.TacPanel
import lt.tacreports.ui.TacTheme
import lt.tacreports.ui.TacType
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

/** An open fill screen; [n] makes a repeated request for the same template start fresh. */
private data class FillRequest(val templateId: String?, val n: Int)

private val fillRequest = MutableStateFlow<FillRequest?>(null)
private var requests = 0

/** Called from Swift by the "New report" App Intent (Shortcuts, Action Button, Back Tap). */
fun openReportPicker() {
    fillRequest.value = FillRequest(null, ++requests)
}

/** The whole iOS app; SwiftUI only hosts this controller. */
fun MainViewController(): UIViewController {
    initStores(IosKeyValueStore)
    return ComposeUIViewController { TacTheme { IosRoot() } }
}

@Composable
private fun IosRoot() {
    val request by fillRequest.collectAsState()
    Box(Modifier.fillMaxSize().background(Tac.Bg)) {
        val r = request
        if (r != null) {
            key(r) {
                FillRoot(initialId = r.templateId, onManage = { fillRequest.value = null }, onDone = { fillRequest.value = null })
            }
        } else {
            ManageRoot(
                version = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "",
                onFill = { fillRequest.value = FillRequest(it, ++requests) },
                quickPanel = { QuickAccessPanel() },
            )
        }
        MessageHost()
    }
}

/** iOS has no bubble; this explains the Action Button / Back Tap route instead. */
@Composable
private fun QuickAccessPanel() {
    val s = strings()
    TacPanel(Modifier.fillMaxWidth()) {
        SectionLabel(s.quickAccess)
        Text(s.quickAccessHelp, style = TacType.Small)
    }
}
