package lt.tacreports

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import lt.tacreports.bubble.BubbleState
import lt.tacreports.model.TemplateStore
import lt.tacreports.ui.FillRoot
import lt.tacreports.ui.TacTheme

/**
 * Translucent screen over whatever app is open: pick a template, fill it in, copy.
 * Finishing returns to that app with the report on the clipboard.
 */
class FillActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(Prefs.localized(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TemplateStore.init(this)
        val id = intent.getStringExtra(EXTRA_ID)
        setContent {
            TacTheme {
                FillRoot(
                    initialId = id,
                    onManage = {
                        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    },
                    onDone = ::finish,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        BubbleState.formVisible.value = true
    }

    override fun onStop() {
        super.onStop()
        BubbleState.formVisible.value = false
    }

    companion object {
        const val EXTRA_ID = "template"
    }
}
