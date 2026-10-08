package lt.tacreports

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import lt.tacreports.bubble.BubblePrefs
import lt.tacreports.bubble.BubbleService
import lt.tacreports.ui.ManageRoot
import lt.tacreports.ui.Tac
import lt.tacreports.ui.TacPanel
import lt.tacreports.ui.TacTheme
import lt.tacreports.ui.TacToggleRow
import lt.tacreports.ui.TacType
import lt.tacreports.ui.importTemplates

/** Template management and settings. Reports themselves are filled in [FillActivity]. */
class MainActivity : ComponentActivity() {
    private lateinit var bubblePrefs: BubblePrefs
    private var bubbleOn by mutableStateOf(false)
    private var overlayAllowed by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initStores(this)
        bubblePrefs = BubblePrefs(this)
        bubbleOn = bubblePrefs.on
        if (savedInstanceState == null) handleShare(intent)

        val version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
        setContent {
            TacTheme {
                ManageRoot(
                    version = version,
                    onFill = { startActivity(Intent(this, FillActivity::class.java).putExtra(FillActivity.EXTRA_ID, it)) },
                    quickPanel = { BubblePanel() },
                )
            }
        }
    }

    @Composable
    private fun BubblePanel() {
        val s = strings()
        TacPanel(Modifier.fillMaxWidth(), color = if (bubbleOn) Tac.Amber else Tac.Cyan) {
            TacToggleRow(s.bubble, bubbleOn, ::setBubble)
            Text(s.bubbleHelp, style = TacType.Small)
            if (bubbleOn && !overlayAllowed) Text(s.overlayNeeded, style = TacType.Small, color = Tac.Amber)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShare(intent)
    }

    override fun onResume() {
        super.onResume()
        overlayAllowed = Settings.canDrawOverlays(this)
        // Back from the "Display over other apps" screen, or a fresh start with the bubble wanted.
        if (bubbleOn && overlayAllowed) BubbleService.start(this)
    }

    private fun setBubble(on: Boolean) {
        bubbleOn = on
        bubblePrefs.on = on
        if (!on) {
            BubbleService.stop(this)
            return
        }
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Settings.canDrawOverlays(this)) {
            BubbleService.start(this)
        } else {
            Toast.makeText(this, currentStrings().overlayNeeded, Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }

    /** A template set shared to the app from a messenger. */
    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        Toast.makeText(this, importTemplates(text, currentStrings()), Toast.LENGTH_SHORT).show()
    }
}
