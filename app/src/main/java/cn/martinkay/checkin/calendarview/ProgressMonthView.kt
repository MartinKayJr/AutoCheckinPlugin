package cn.martinkay.checkin.calendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import cn.martinkay.checkin.model.CalendarScheme
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView

/**
 * 精美进度风格
 * Created by huanghaibin on 2018/2/8.
 */
class ProgressMonthView(context: Context) : MonthView(context) {

    private var radius = 0

    private val progressPaint = Paint()
    private val noneProgressPaint = Paint()
    private val forbiddenPaint = Paint()
    private val futurePaint = Paint()

    init {
        progressPaint.isAntiAlias = true
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = dipToPx(context, 2.2f).toFloat()
        progressPaint.color = -0x440ab600

        noneProgressPaint.isAntiAlias = true
        noneProgressPaint.style = Paint.Style.STROKE
        noneProgressPaint.strokeWidth = dipToPx(context, 2.2f).toFloat()
        noneProgressPaint.color = -0x6f303031

        forbiddenPaint.isAntiAlias = true
        forbiddenPaint.style = Paint.Style.FILL
        forbiddenPaint.color = (0xffff4444).toInt()

        android.R.color.holo_green_light
        futurePaint.isAntiAlias = true
        futurePaint.style = Paint.Style.FILL
        futurePaint.color = (0xff99cc00).toInt()
    }

    override fun onLongClick(v: View?): Boolean {
        val result = super.onLongClick(v)
        // 强制刷新一次，不然外部拦截不会走到parent内部的 invalidate
        if (result) {
            invalidate()
        }
        return result
    }

    override fun onPreviewHook() {
        radius = mItemWidth.coerceAtMost(mItemHeight) / 11 * 4
    }

    override fun onDrawSelected(
        canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean
    ): Boolean {
        //val cx = x + mItemWidth / 2
        //val cy = y + mItemHeight / 2
        //canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val scheme = calendar.scheme

        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        when (scheme) {
            CalendarScheme.AUTO_SIGN_DAY_ALLOW -> {
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), futurePaint)
            }

            CalendarScheme.AUTO_SIGN_DAY_FORBIDDEN -> {
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), forbiddenPaint)
            }

            else -> {
                val angle = getAngle(calendar.scheme)
                if (angle != 0) {
                    val progressRectF = RectF(
                        (cx - radius).toFloat(),
                        (cy - radius).toFloat(),
                        (cx + radius).toFloat(),
                        (cy + radius).toFloat()
                    )
                    canvas.drawArc(progressRectF, -90f, angle.toFloat(), false, progressPaint)
                    val noneRectF = RectF(
                        (cx - radius).toFloat(),
                        (cy - radius).toFloat(),
                        (cx + radius).toFloat(),
                        (cy + radius).toFloat()
                    )
                    canvas.drawArc(
                        noneRectF,
                        (angle - 90).toFloat(),
                        (360 - angle).toFloat(),
                        false,
                        noneProgressPaint
                    )
                }
            }
        }
    }

    override fun onDrawText(
        canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean
    ) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2
        canvas.drawText(
            calendar.day.toString(), cx.toFloat(), baselineY, mCurMonthTextPaint
        )
    }

    companion object {
        /**
         * 获取角度
         *
         * @param progress 进度
         * @return 获取角度
         */
        private fun getAngle(scheme: String): Int {
            return when (scheme) {
                CalendarScheme.AUTO_SIGN_COUNT_1 -> 90
                CalendarScheme.AUTO_SIGN_COUNT_2 -> 180
                CalendarScheme.AUTO_SIGN_COUNT_3 -> 270
                CalendarScheme.AUTO_SIGN_COUNT_4 -> 360
                else -> 0
            }
        }

        /**
         * dp转px
         *
         * @param context context
         * @param dpValue dp
         * @return px
         */
        private fun dipToPx(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}