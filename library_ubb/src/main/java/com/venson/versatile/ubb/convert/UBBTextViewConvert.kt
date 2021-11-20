package com.venson.versatile.ubb.convert

import android.graphics.Paint
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.widget.UBBTextView

open class UBBTextViewConvert(private val textView: UBBTextView) : AbstractConvert() {

    override fun resetContent() {
        textView.setSpannableText("")
    }

    override fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String?,
        align: Paint.Align
    ) {
        textView.insertSpan(span, start, end, content, align)
    }

    override fun onStyleParsed(style: AbstractStyle, start: Int, end: Int, align: Paint.Align) {
        textView.insertSpan(style.getSpan(), start, end, style.getSpanText(), align)
    }

    override fun getContentLength(): Int {
        return textView.length()
    }
}