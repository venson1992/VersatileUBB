package com.venson.versatile.ubb.span

import android.view.View
import com.venson.versatile.ubb.style.AbstractStyle

interface ISpan {

    /**
     * 获取数据
     */
    fun getStyle(): AbstractStyle

    fun getTagName(): String = getStyle().getTagName()

    /**
     * 获取当前样式的ubb代码
     */
    fun getUBB(): String

    /**
     * 获取当前样式的文本内容
     */
    fun getText(): String = getStyle().getText()

    /**
     * 获取应用span后显示得文本内容
     */
    fun getSpanText(): String = getStyle().getSpanText()

    /**
     * 该样式前置是否另起一行
     */
    fun isStartSingleLine(): Boolean = false

    /**
     * 该样式后置是否另起一行
     */
    fun isEndSingleLine(): Boolean = false

    /**
     * 获取点击事件
     */
    fun getClickListener(): View.OnClickListener? = null
}