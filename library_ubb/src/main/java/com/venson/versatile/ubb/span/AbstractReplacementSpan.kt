package com.venson.versatile.ubb.span

import android.text.style.ReplacementSpan
import com.venson.versatile.ubb.style.AbstractStyle

abstract class AbstractReplacementSpan(private val abstractStyle: AbstractStyle) :
    ReplacementSpan(), ISpan {

    override fun getStyle(): AbstractStyle {
        return abstractStyle
    }

    override fun getUBB(): String = getStyle().toUBB(false)

    /**
     * 该样式前置是否另起一行
     */
    override fun isStartSingleLine(): Boolean = false

    /**
     * 该样式后置是否另起一行
     */
    override fun isEndSingleLine(): Boolean = false
}