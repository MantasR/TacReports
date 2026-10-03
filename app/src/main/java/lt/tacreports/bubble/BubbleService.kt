package lt.tacreports.bubble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import lt.tacreports.FillActivity
import lt.tacreports.MainActivity
import lt.tacreports.Prefs
import lt.tacreports.R
import kotlin.math.abs

/**
 * Keeps the floating bubble over other apps. Tapping it opens [FillActivity]; dragging moves it,
 * and it snaps to the nearest side. Runs as a foreground service with a "Stop" notification so
 * Android doesn't remove the bubble while another app is in front.
 */
class BubbleService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var prefs: Prefs
    private var bubble: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(Prefs.localized(newBase))

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WindowManager::class.java)
        prefs = Prefs(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP || !Settings.canDrawOverlays(this)) {
            if (intent?.action == ACTION_STOP) prefs.bubbleOn = false
            stopSelf()
            return START_NOT_STICKY
        }
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        runCatching { ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(), type) }
            .onFailure { stopSelf(); return START_NOT_STICKY }
        if (bubble == null) show()
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        bubble?.let { runCatching { wm.removeView(it) } }
        bubble = null
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun show() {
        val size = (56 * resources.displayMetrics.density).toInt()
        params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            val screenW = resources.displayMetrics.widthPixels
            x = prefs.bubbleX.takeIf { it >= 0 } ?: (screenW - size)
            y = prefs.bubbleY
        }
        val view = BubbleView(this)
        view.contentDescription = getString(R.string.bubble_open)
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var dragging = false
        view.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY; startX = params.x; startY = params.y; dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (!dragging && (abs(dx) > slop || abs(dy) > slop)) dragging = true
                    if (dragging) {
                        params.x = (startX + dx).toInt()
                        params.y = (startY + dy).toInt().coerceAtLeast(0)
                        runCatching { wm.updateViewLayout(view, params) }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val screenW = resources.displayMetrics.widthPixels
                        params.x = if (params.x + size / 2 < screenW / 2) 0 else screenW - size
                        runCatching { wm.updateViewLayout(view, params) }
                        prefs.bubbleX = params.x
                        prefs.bubbleY = params.y
                    } else {
                        openForm()
                    }
                }
            }
            true
        }
        wm.addView(view, params)
        bubble = view
        scope.launch {
            BubbleState.formVisible.collect { open -> view.visibility = if (open) View.GONE else View.VISIBLE }
        }
    }

    private fun openForm() {
        startActivity(
            Intent(this, FillActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
    }

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, getString(R.string.bubble_channel), NotificationManager.IMPORTANCE_LOW),
            )
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val stop = PendingIntent.getService(this, 1, Intent(this, BubbleService::class.java).setAction(ACTION_STOP), flags)
        val open = PendingIntent.getActivity(this, 2, Intent(this, MainActivity::class.java), flags)
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_bubble)
            .setContentTitle(getString(R.string.bubble_notification))
            .setContentText(getString(R.string.bubble_notification_text))
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null as android.graphics.drawable.Icon?, getString(R.string.bubble_stop), stop).build())
            .build()
    }

    companion object {
        private const val CHANNEL = "bubble"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "lt.tacreports.STOP_BUBBLE"

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            runCatching { context.startForegroundService(Intent(context, BubbleService::class.java)) }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BubbleService::class.java))
        }
    }
}
