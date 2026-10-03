package lt.tacreports

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import lt.tacreports.bubble.BubbleService
import lt.tacreports.model.Template
import lt.tacreports.model.TemplateJson
import lt.tacreports.model.TemplateStore
import lt.tacreports.ui.EditorScreen
import lt.tacreports.ui.HomeActions
import lt.tacreports.ui.HomeScreen
import lt.tacreports.ui.Tac
import lt.tacreports.ui.TacTheme

/** Template management and settings. Reports themselves are filled in [FillActivity]. */
class MainActivity : ComponentActivity() {
    private lateinit var prefs: Prefs
    private var bubbleOn by mutableStateOf(false)
    private var overlayAllowed by mutableStateOf(false)
    private var mgrsDigits by mutableIntStateOf(5)
    private var editing by mutableStateOf<String?>(null)

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(Prefs.localized(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = Prefs(this)
        TemplateStore.init(this)
        bubbleOn = prefs.bubbleOn
        mgrsDigits = prefs.mgrsDigits
        editing = savedInstanceState?.getString("editing")
        if (savedInstanceState == null) handleShare(intent)

        val version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
        setContent {
            TacTheme {
                Box(Modifier.fillMaxSize().background(Tac.Bg)) {
                    val id = editing
                    val template = id?.let { TemplateStore.get(it) }
                    if (template != null) {
                        EditorScreen(
                            template,
                            onBack = { editing = null },
                            onShare = { share(listOf(it)) },
                            onDuplicate = { t ->
                                TemplateStore.duplicate(t.id, getString(R.string.copy_suffix))?.let { editing = it.id }
                            },
                        )
                    } else {
                        HomeScreen(
                            bubbleOn = bubbleOn,
                            overlayAllowed = overlayAllowed,
                            language = prefs.language,
                            mgrsDigits = mgrsDigits,
                            version = version,
                            actions = HomeActions(
                                onBubble = ::setBubble,
                                onFill = { startActivity(Intent(this, FillActivity::class.java).putExtra(FillActivity.EXTRA_ID, it)) },
                                onEdit = { editing = it },
                                onNew = {
                                    val t = Template(name = getString(R.string.new_template_name))
                                    TemplateStore.put(t)
                                    editing = t.id
                                },
                                onImport = ::importFromClipboard,
                                onShareAll = { share(TemplateStore.templates.value) },
                                onLanguage = { lang ->
                                    prefs.language = lang
                                    recreate()
                                },
                                onMgrsDigits = { mgrsDigits = it; prefs.mgrsDigits = it },
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("editing", editing)
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
        prefs.bubbleOn = on
        if (!on) {
            BubbleService.stop(this)
            return
        }
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Settings.canDrawOverlays(this)) {
            BubbleService.start(this)
        } else {
            Toast.makeText(this, R.string.overlay_needed, Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }

    private fun share(templates: List<Template>) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, TemplateJson.toJson(templates))
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }

    private fun importFromClipboard() {
        val clip = getSystemService(ClipboardManager::class.java).primaryClip
        val text = clip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        importText(text)
    }

    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let(::importText)
    }

    private fun importText(text: String) {
        val list = TemplateJson.fromJson(text)
        val msg = if (list.isNullOrEmpty()) getString(R.string.import_none) else {
            getString(R.string.imported, TemplateStore.import(list))
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
