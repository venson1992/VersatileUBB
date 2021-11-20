package com.venson.versatile.ubb.span

import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import com.venson.versatile.ubb.style.AbstractStyle

/**
 * 可 Touch 的 Span，在 [.setPressed] 后根据是否 pressed 来触发不同的UI状态
 *
 *
 * 提供设置 span 的文字颜色和背景颜色的功能, 在构造时传入
 *
 */
abstract class TouchableSpan(
    @field:ColorInt @param:ColorInt var normalTextColor: Int,
    @field:ColorInt @param:ColorInt var pressedTextColor: Int,
    @field:ColorInt @param:ColorInt val normalBackgroundColor: Int,
    @field:ColorInt @param:ColorInt val pressedBackgroundColor: Int
) : ClickableSpan(), ITouchableSpan, ISpan {
    private var mIsPressed = false

    var isNeedUnderline = false

    override fun onClick(widget: View) {
        if (ViewCompat.isAttachedToWindow(widget)) {
            onSpanClick(widget)
        }
    }

    override fun setPressed(isSelected: Boolean) {
        mIsPressed = isSelected
    }

    fun isPressed(): Boolean {
        return mIsPressed
    }

    override fun getStyle(): AbstractStyle {
        TODO("Not yet implemented")
    }
}