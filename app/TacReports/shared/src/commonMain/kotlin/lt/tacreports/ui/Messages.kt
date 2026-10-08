package lt.tacreports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

/** Short in-app messages for platforms without toasts (iOS). */
object Messages {
    /** The message plus a counter, so the same text shown twice restarts its timer. */
    internal val current = MutableStateFlow<Pair<String, Int>?>(null)
    private var n = 0

    fun show(text: String) {
        current.value = text to ++n
    }
}

/** Shows [Messages] as a bracketed banner at the bottom for a few seconds. */
@Composable
fun BoxScope.MessageHost() {
    val msg by Messages.current.collectAsState()
    val m = msg ?: return
    LaunchedEffect(m) {
        delay(2500)
        if (Messages.current.value == m) Messages.current.value = null
    }
    Box(Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(16.dp).fillMaxWidth()) {
        TacPanel(Modifier.fillMaxWidth().background(Tac.Bg), color = Tac.Amber) {
            Text(m.first, style = TacType.Body)
        }
    }
}
