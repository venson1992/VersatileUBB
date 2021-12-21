package com.venson.versatile.ubb.style

import android.graphics.Paint
import com.venson.versatile.ubb.span.AtSomeoneSpan

/**
 * AT用户样式
 */
class AtSomeoneStyle(private val align: Paint.Align = Paint.Align.LEFT) : AbstractStyle() {

    companion object {
        internal const val TAG_NAME = "user"
        internal const val ATTR_USER = "user"
    }

    override fun getTagName(): String = TAG_NAME

    override fun getSpan(): Any {
        return AtSomeoneSpan(this, align)
    }

    override fun getSpanText(): String {
        return "@" + super.getSpanText()
    }

}