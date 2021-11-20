package com.venson.versatile.ubb.fix

import android.text.Selection
import android.text.Spannable
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import com.venson.versatile.ubb.BuildConfig
import com.venson.versatile.ubb.span.ITouchableSpan
import java.lang.ref.WeakReference

class TouchDecorHelper {

    private var mPressedSpanRf: WeakReference<ITouchableSpan>? = null

    fun onTouchEvent(textView: TextView, spannable: Spannable, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val span = getPressedSpan(textView, spannable, event)
                if (span != null) {
                    span.setPressed(true)
                    Selection.setSelection(
                        spannable,
                        spannable.getSpanStart(span),
                        spannable.getSpanEnd(span)
                    )
                    mPressedSpanRf =
                        WeakReference(span)
                }
                if (textView is ISpanTouchFix) {
                    val tv = textView as ISpanTouchFix
                    tv.setTouchSpanHit(span != null)
                }
                span != null
            }
            MotionEvent.ACTION_MOVE -> {
                val touchedSpan = getPressedSpan(textView, spannable, event)
                var recordSpan: ITouchableSpan? = null
                if (mPressedSpanRf != null) {
                    recordSpan = mPressedSpanRf!!.get()
                }
                if (recordSpan != null && recordSpan !== touchedSpan) {
                    recordSpan.setPressed(false)
                    mPressedSpanRf = null
                    recordSpan = null
                    Selection.removeSelection(spannable)
                }
                if (textView is ISpanTouchFix) {
                    val tv = textView as ISpanTouchFix
                    tv.setTouchSpanHit(recordSpan != null)
                }
                recordSpan != null
            }
            MotionEvent.ACTION_UP -> {
                var touchSpanHint = false
                var recordSpan: ITouchableSpan? = null
                if (mPressedSpanRf != null) {
                    recordSpan = mPressedSpanRf!!.get()
                }
                if (recordSpan != null) {
                    touchSpanHint = true
                    recordSpan.setPressed(false)
                    if (event.action == MotionEvent.ACTION_UP) {
                        recordSpan.onSpanClick(textView)
                    }
                }
                mPressedSpanRf = null
                Selection.removeSelection(spannable)
                if (textView is ISpanTouchFix) {
                    val tv = textView as ISpanTouchFix
                    tv.setTouchSpanHit(touchSpanHint)
                }
                touchSpanHint
            }
            else -> {
                var recordSpan: ITouchableSpan? = null
                if (mPressedSpanRf != null) {
                    recordSpan = mPressedSpanRf!!.get()
                }
                recordSpan?.setPressed(false)
                if (textView is ISpanTouchFix) {
                    val tv = textView as ISpanTouchFix
                    tv.setTouchSpanHit(false)
                }
                mPressedSpanRf = null
                Selection.removeSelection(spannable)
                false
            }
        }
    }

    fun getPressedSpan(
        textView: TextView,
        spannable: Spannable,
        event: MotionEvent
    ): ITouchableSpan? {
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= textView.totalPaddingLeft
        y -= textView.totalPaddingTop
        x += textView.scrollX
        y += textView.scrollY
        val layout = textView.layout
        val line = layout.getLineForVertical(y)

        /*
         * BugFix: https://issuetracker.google.com/issues/113348914
         */
        try {
            var off = layout.getOffsetForHorizontal(line, x.toFloat())
            if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) {
                // 实际上没点到任何内容
                off = -1
            }
            val link = spannable.getSpans(off, off, ITouchableSpan::class.java)
            var touchedSpan: ITouchableSpan? = null
            if (link.isNotEmpty()) {
                touchedSpan = link[0]
            }
            return touchedSpan
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) {
                Log.d(this.toString(), "getPressedSpan", e)
            }
        }
        return null
    }
}