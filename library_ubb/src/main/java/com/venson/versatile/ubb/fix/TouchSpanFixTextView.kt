package com.venson.versatile.ubb.fix

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * 修复了 [android.widget.TextView] 与 [android.text.style.ClickableSpan] 一起使用时，
 * 点击 [android.text.style.ClickableSpan] 也会触发 [android.widget.TextView] 的事件的问题。
 *
 * 同时通过 [setNeedForceEventToParent] 控制该 TextView 的点击事件能否传递给其 Parent，
 * 修复了 [android.widget.TextView] 默认情况下
 * 如果添加了 [android.text.style.ClickableSpan] 之后
 * 就无法把点击事件传递给 [android.widget.TextView] 的 Parent 的问题。
 *
 * 注意: 使用该 [android.widget.TextView] 时,
 * 用 [com.aiwu.market.ubb.span.TouchableSpan] 代替 [android.text.style.ClickableSpan],
 * 且同时可以使用 [com.aiwu.market.ubb.span.TouchableSpan] 达到修改 span 的文字颜色和背景色的目的。
 *
 * 注意: 使用该 [android.widget.TextView] 时,
 * 需调用 [setMovementMethodDefault] 方法设置默认的 [com.aiwu.market.ubb.fix.TouchMovementMethod],
 * TextView 会在 [onTouchEvent] 时将事件传递给 [com.aiwu.market.ubb.fix.TouchMovementMethod],
 * 然后传递给 [com.aiwu.market.ubb.span.TouchableSpan], 实现点击态的变化和点击事件的响应。
 *
 * @see com.aiwu.market.ubb.span.TouchableSpan
 *
 * @see com.aiwu.market.ubb.fix.TouchMovementMethod
 */
open class TouchSpanFixTextView : androidx.appcompat.widget.AppCompatTextView, ISpanTouchFix {

    /**
     * 记录当前 Touch 事件对应的点是不是点在了 span 上面
     */
    private var mTouchSpanHit = false

    /**
     * 记录每次真正传入的press，每次更改mTouchSpanHint，需要再调用一次setPressed，确保press状态正确
     */
    private var mIsPressedRecord = false

    /**
     * TextView是否应该消耗事件
     */
    private var mNeedForceEventToParent = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, android.R.attr.textViewStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

//    init {
//        highlightColor = Color.TRANSPARENT
//    }

    fun setNeedForceEventToParent(needForceEventToParent: Boolean) {
        mNeedForceEventToParent = needForceEventToParent
        isFocusable = !needForceEventToParent
        isClickable = !needForceEventToParent
        isLongClickable = !needForceEventToParent
    }

    /**
     * 使用者主动调用
     */
    fun setMovementMethodDefault() {
        setMovementMethodCompat(TouchMovementMethod.get())
    }

    fun setMovementMethodCompat(movement: MovementMethod?) {
        movementMethod = movement
        if (mNeedForceEventToParent) {
            setNeedForceEventToParent(true)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (text !is Spannable || movementMethod !is TouchMovementMethod) {
            mTouchSpanHit = false
            return super.onTouchEvent(event)
        }
        mTouchSpanHit = true
        // 调用super.onTouchEvent,会走到QMUILinkTouchMovementMethod
        // 会走到QMUILinkTouchMovementMethod#onTouchEvent会修改mTouchSpanHint
        val ret = super.onTouchEvent(event)
        return if (mNeedForceEventToParent) {
            mTouchSpanHit
        } else ret
    }

    override fun setTouchSpanHit(hit: Boolean) {
        if (mTouchSpanHit != hit) {
            mTouchSpanHit = hit
            isPressed = mIsPressedRecord
        }
    }

    override fun performClick(): Boolean {
        return if (!mTouchSpanHit && !mNeedForceEventToParent) {
            super.performClick()
        } else {
            false
        }
    }

    override fun performLongClick(): Boolean {
        return if (!mTouchSpanHit && !mNeedForceEventToParent) {
            super.performLongClick()
        } else {
            false
        }
    }

    override fun setPressed(pressed: Boolean) {
        mIsPressedRecord = pressed
        if (!mTouchSpanHit) {
            onSetPressed(pressed)
        }
    }

    protected fun onSetPressed(pressed: Boolean) {
        super.setPressed(pressed)
    }
}