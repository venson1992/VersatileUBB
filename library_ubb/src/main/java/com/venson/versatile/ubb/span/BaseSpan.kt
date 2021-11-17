package com.venson.versatile.ubb.span

import android.text.style.ReplacementSpan

abstract class BaseSpan(private val spanContent: SpanContent) : ReplacementSpan(), ISpan {

    override fun getSpanContent(): SpanContent {
        return spanContent
    }

    /**
     * 获取当前样式的ubb代码
     */
    override fun getUBB(): String {
        return spanContent.toUBB(getTag())
    }

    /**
     * 获取当前样式的文本内容
     */
    override fun getText(): String {
        return spanContent.getText()
    }

    /**
     * 该样式前置是否另起一行
     */
    override fun isStartSingleLine(): Boolean = false

    /**
     * 该样式后置是否另起一行
     */
    override fun isEndSingleLine(): Boolean = false
}