package com.venson.versatile.ubb.fix

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.method.Touch
import android.view.MotionEvent
import android.widget.TextView

/**
 * 配合 [TouchDecorHelper] 使用
 */
class TouchMovementMethod private constructor() : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        return (sHelper.onTouchEvent(widget, buffer, event)
                || Touch.onTouchEvent(widget, buffer, event))
    }

    companion object {

        private var sInstance: MovementMethod? = null
            get() {
                if (field == null) {
                    field = TouchMovementMethod()
                }
                return field
            }

        fun get(): MovementMethod {
            return sInstance!!
        }

        private val sHelper = TouchDecorHelper()
    }
}