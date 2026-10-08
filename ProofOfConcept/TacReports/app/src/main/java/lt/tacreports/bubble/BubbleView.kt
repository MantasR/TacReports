package lt.tacreports.bubble

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/** The round floating button: a dark disc with a cyan ring and a report-sheet glyph. */
class BubbleView(context: Context) : View(context) {
    private val d = resources.displayMetrics.density
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xF0041719.toInt() }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = 0x552EE6D0; strokeWidth = 5 * d
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = 0xFF2EE6D0.toInt(); strokeWidth = 1.8f * d
    }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = 0xFFCFFFF8.toInt(); strokeWidth = 2 * d; strokeCap = Paint.Cap.ROUND
    }
    private val amber = Paint(line).apply { color = 0xFFFFB000.toInt() }

    override fun onDraw(c: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = minOf(cx, cy) - 4 * d
        c.drawCircle(cx, cy, r, fill)
        c.drawCircle(cx, cy, r, glow)
        c.drawCircle(cx, cy, r, ring)
        // Sheet outline with three text lines; the first is amber.
        val w = r * 0.85f
        val h = r * 1.1f
        val sheet = RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)
        c.drawRect(sheet, line)
        val x0 = sheet.left + w * 0.2f
        val x1 = sheet.right - w * 0.2f
        for (i in 0..2) {
            val y = sheet.top + h * (0.28f + i * 0.22f)
            c.drawLine(x0, y, if (i == 2) x0 + (x1 - x0) * 0.6f else x1, y, if (i == 0) amber else line)
        }
    }
}
