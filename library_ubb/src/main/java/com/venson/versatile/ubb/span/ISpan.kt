package com.venson.versatile.ubb.span

import android.view.View

interface ISpan {

    /**
     * UBB标签
     */
    fun getTag(): String

    /**
     * 获取数据
     */
    fun getSpanContent(): SpanContent

    /**
     * 获取当前样式的ubb代码
     */
    fun getUBB(): String

    /**
     * 获取当前样式的文本内容
     */
    fun getText(): String

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